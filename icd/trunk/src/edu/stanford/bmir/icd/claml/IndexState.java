package edu.stanford.bmir.icd.claml;

enum IndexState {
    VALID, // index is in a valid state
    INVALID,  // index is invalid
    NEW; // index does not exist in the ontology
}