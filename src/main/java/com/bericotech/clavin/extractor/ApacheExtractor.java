package com.bericotech.clavin.extractor;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSDictionary;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*#####################################################################
 * 
 * CLAVIN (Cartographic Location And Vicinity INdexer)
 * ---------------------------------------------------
 * 
 * Copyright (C) 2012-2013 Berico Technologies
 * http://clavin.bericotechnologies.com
 * 
 * ====================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * ====================================================================
 * 
 * ApacheExtractor.java
 * 
 *###################################################################*/

/**
 * Extracts location names from unstructured text documents using a
 * named entity recognizer (Apache OpenNLP Name Finder).
 *
 */
public class ApacheExtractor implements LocationExtractor {
    
    // the actual named entity recognizer (NER) object
    private NameFinderME nameFinder;
    
    // used to tokenize plain text into the OpenNLP format
    private TokenizerME tokenizer;

    // used to split the input into sentences before finding names
    private SentenceDetectorME sentenceDetector;

    // experimental - try to grab noun phrases for locations
    private POSTaggerME posTagger;

    // experimental - determine what options a token has for POS,
    //  perhaps to prefer proper nouns as locations
    private POSDictionary posDictionary;

    // resource files used by Apache OpenNLP Name Finder
    private static final String pathToNERModel = "/en-ner-location.bin";
    private static final String pathToTokenizerModel = "/en-token.bin";
    private static final String pathToSentenceDetectorModel = "/en-sent.bin";
    private static final String pathToPOSDetectorModel = "/en-pos-maxent.bin";
    private static final String pathToPOSDictionary = "/en-tagdict.xml";

    
    /**
     * Builds an {@link ApacheExtractor} by instantiating the OpenNLP
     * Name Finder and Tokenizer.
     * 
     * @throws IOException 
     */
    public ApacheExtractor() throws IOException {
        nameFinder = new NameFinderME(new TokenNameFinderModel(ApacheExtractor.class.getResourceAsStream(pathToNERModel)));
        tokenizer = new TokenizerME(new TokenizerModel(ApacheExtractor.class.getResourceAsStream(pathToTokenizerModel)));
        sentenceDetector = new SentenceDetectorME(new SentenceModel(ApacheExtractor.class.getResourceAsStream(pathToSentenceDetectorModel)));
        posTagger = new POSTaggerME(new POSModel(ApacheExtractor.class.getResourceAsStream(pathToPOSDetectorModel)));
        posDictionary = POSDictionary.create(ApacheExtractor.class.getResourceAsStream(pathToPOSDictionary));
    }
    
    /**
     * Extracts location names from unstructured text using the named
     * entity recognizer (NER) feature provided by the Apache OpenNLP
     * Name Finder.
     * 
     * @param plainText     Contents of text document
     * @return List of location names and positions
     */
    public List<LocationOccurrence> extractLocationNames(String plainText) {
        if(plainText == null) {
            throw new IllegalArgumentException("plaintext input to extractLocationNames should not be null");
        }

        List<LocationOccurrence> nerResults = new ArrayList<LocationOccurrence>();

        // The values used in these Spans are string character offsets
        Span sentenceSpans[] = sentenceDetector.sentPosDetect(plainText);

        // Each sentence gets processed on its own
        for (Span sentenceSpan : sentenceSpans) {

            // find the start and end position of this sentence in the document
            String sentence = plainText.substring(sentenceSpan.getStart(), sentenceSpan.getEnd());

            // tokenize the text into the required OpenNLP format
            String[] tokens = tokenizer.tokenize(sentence);

            //the values used in these Spans are string character offsets of each token from the sentence beginning
            Span[] tokenPositionsWithinSentence = tokenizer.tokenizePos(sentence);

            // First use the name finder to get locations
            {
                // find the location names in the tokenized text
                // the values used in these Spans are NOT string character offsets, they are indices into the 'tokens' array
                Span names[] = nameFinder.find(tokens);


                //for each name that got found, create our corresponding occurrence
                for (Span name : names) {

                    //find offsets relative to the start of the sentence
                    int beginningOfFirstWord = tokenPositionsWithinSentence[name.getStart()].getStart();
                    // -1 because the high end of a Span is noninclusive
                    int endOfLastWord = tokenPositionsWithinSentence[name.getEnd() - 1].getEnd();

                    //to get offsets relative to the document as a whole, just add the offset for the sentence itself
                    int startOffsetInDoc = sentenceSpan.getStart() + beginningOfFirstWord;
                    int endOffsetInDoc = sentenceSpan.getStart() + endOfLastWord;

                    //look back into the original input string to figure out what the text is that I got a hit on
                    String nameInDocument = plainText.substring(startOffsetInDoc, endOffsetInDoc);

                    // add to List of results to return
                    nerResults.add(new LocationOccurrence(nameInDocument, startOffsetInDoc));
                }

                // this is necessary to maintain consistent results across
                // multiple runs on the same data, which is what we want
                nameFinder.clearAdaptiveData();
            }

            // Then look for tokens that are proper nouns (NNP) according to the dictionary
            {
                // tag the text with parts of speech
                String tags[] = posTagger.tag(tokens);

                Span[] spans = tokenizer.tokenizePos(sentence);

                for( int i=0; i<tokens.length; i++ ) {
                    if( tags[i].equalsIgnoreCase("NNP") ) {
                        String[] allTagsForWord = posDictionary.getTags(tokens[i]);
                        if( allTagsForWord != null && allTagsForWord.length == 1 ) {
                            // This word is only a proper noun, so add it to the candidate list.

                            //find offsets relative to the start of the sentence
                            int beginningOfFirstWord = tokenPositionsWithinSentence[i].getStart();
                            // -1 because the high end of a Span is noninclusive
                            int endOfLastWord = tokenPositionsWithinSentence[i].getEnd();

                            //to get offsets relative to the document as a whole, just add the offset for the sentence itself
                            int startOffsetInDoc = sentenceSpan.getStart() + beginningOfFirstWord;
                            int endOffsetInDoc = sentenceSpan.getStart() + endOfLastWord;

                            //look back into the original input string to figure out what the text is that I got a hit on
                            String nameInDocument = plainText.substring(startOffsetInDoc, endOffsetInDoc);

                            // add to List of results to return
                            nerResults.add(new LocationOccurrence(nameInDocument, startOffsetInDoc));
                        }
                    }
                }
            }
        }

        return nerResults;
    }

}
