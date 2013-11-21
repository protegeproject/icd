{\rtf1\ansi\ansicpg1252\cocoartf1187\cocoasubrtf370
{\fonttbl\f0\froman\fcharset0 Times-Roman;}
{\colortbl;\red255\green255\blue255;\red42\green0\blue255;}
\margl1440\margr1440\vieww14180\viewh10800\viewkind0
\pard\tx720\tx1440\tx2160\tx2880\tx3600\tx4320\tx5040\tx5760\tx6480\tx7200\tx7920\tx8640\pardirnatural

\f0\fs24 \cf0 Here is the instructions about how to run mTurk project. If you have any problem, please email yunlou@stanford.edu\
\
1. install mongoDB from {\field{\*\fldinst{HYPERLINK "http://www.mongodb.org/"}}{\fldrslt http://www.mongodb.org/}}\
2. run mongoDB with your local host. Mturk connects to MongoDB from local host.\
3. since we need to know whether a disease or symptom is on Wikipedia, we need to download the English version of Wikipedia from {\field{\*\fldinst{HYPERLINK "http://en.wikipedia.org/wiki/Wikipedia:Database_download"}}{\fldrslt http://en.wikipedia.org/wiki/Wikipedia:Database_download}}.\
4. edit the wikipedia file path in FindBranch class in default package, and run the class FindBranch\
5. in the main function of prepareData in Experiment package,  set the path to your disease ontology, and claim the root of your experimental branch in the ontology.\
6. install \cf2 java-aws-mturk-1.6.2\cf0 , set \cf2 properties\cf0  file according the instructions.\
7. edit path to \cf2 java-aws-mturk-1.6.2\cf0  in interface class in Exp3 package to be the location where you installed your \cf2 java-aws-mturk-1.6.2\cf0 .\
8. run the run class in Exp3 to start experiment}