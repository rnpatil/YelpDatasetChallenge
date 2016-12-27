'''
    Author:
    Jay Nagle (jaynagle)
    Sruthi Vani (srameshv)
'''
import numpy as np
from pymongo import MongoClient
from contextlib import closing
from multiprocessing import cpu_count,Pool
import pandas as pd
import itertools
from nltk.stem.snowball import SnowballStemmer
from nltk.corpus import stopwords as stpwrds
from re import sub as regexsub
from sklearn.feature_extraction.text import HashingVectorizer, TfidfVectorizer, CountVectorizer
from sklearn.linear_model import SGDClassifier
from sklearn.linear_model import PassiveAggressiveClassifier
from sklearn.linear_model import Perceptron
from sklearn.naive_bayes import MultinomialNB
from sklearn.svm import SVC
from collections import Counter, defaultdict
from random import sample
from math import floor,ceil
from sklearn.preprocessing import MultiLabelBinarizer
from matplotlib import pyplot as plt
from sklearn import datasets

client = MongoClient('localhost', 27017)
db = client.yelp

training_data = []
batch_size = 1000
n_test_documents = 1000

stopwords = set(stpwrds.words('english'))
stemmer = SnowballStemmer('english')

def strip_symbols(text):
    return(regexsub('[^A-Za-z0-9\. ]+', '', text).replace("."," "))
    
def stem_and_remove_stop_words(text):
    symbolsRemoved = strip_symbols(text)
    stemmed=" ".join([stemmer.stem(token) for token in symbolsRemoved.split(" ") if token != "" and token not in stopwords])
    return(stemmed)

def log_probability_filter(ndarray,threshold):
    filteredList=[]
    for row in ndarray:
        filteredList.append(list(np.flatnonzero(row > threshold)))
    return(filteredList)
    
def calculate_accuracy(test_labels, predicted_labels):
    correct_prediction_count = 0
    for i, val in enumerate(test_labels):
        if int(val) == predicted_labels[i]:
            correct_prediction_count += 1

    return float(correct_prediction_count) / len(test_labels)

'''
    F-score = 2 * (precision*recall / precision + recall)
    precision = # of correctly predicted items / total predictions
    recall = # of correctly predicted items / actual predictions
'''
def calculateFScore(Y_actual,Y_predicted):
    fscore=0
    allPrecision=[]
    allRecall=[]
    for i in range(len(Y_actual)):
        actual=Y_actual[i]
        predicted=Y_predicted[i]
        precision=0
        recall=0
        for label in actual:
            if label in predicted:
                precision+=1
        if len(predicted)==0:
            precision=0
        else:
            precision=precision/len(predicted)
        allPrecision.append(precision)
        for label in predicted:
            if label in actual:
                recall+=1
        recall=recall/len(actual)
        allRecall.append(recall)
    precision=sum(allPrecision)/len(allPrecision)
    recall=sum(allRecall)/len(allRecall)
    if precision==0 and recall==0:
        fscore=0
    else:
        fscore=2 *((precision*recall)/(precision+recall))
    return((fscore,precision,recall))    
    
def expand_categories(list):
    X=[]
    Y=[]
    for item in list:
        for category in item[0]:
            Y.append(category)
            X.append(item[1])
    return ((X,Y))
    
def transform_to_tuple(x,y):
    return(x,y)

    
def fit_estimators(tupleofestimators):
    estimator,trainX,trainY=tupleofestimators
    rt=[estimator.fit(trainX,trainY)]
    return(rt)
    
def predict(tupleofestimators):
    estimator,predict=tupleofestimators
    return(estimator.predict_log_proba(predict))

def load_training_data():
    
    reviews = db.reviews.find()
    tips = db.tips.find()
    
    cleaned_data = []
    for i in reviews:
        cleaned_data.append((i['categories'], stem_and_remove_stop_words(i['text'])))
        
    for i in tips:
        cleaned_data.append((i['categories'], stem_and_remove_stop_words(i['text'])))    
     
    whole_X=[]
    whole_Y=[]
    vocabulary=set()
    locationDict=defaultdict(list)
    for i in range(len(cleaned_data)):
        whole_X.append(cleaned_data[i][1])
        whole_Y.append(cleaned_data[i][0])
        locationDict["".join(set(cleaned_data[i][0]))].append(i)
        for item in cleaned_data[i][0]:
            if item not in vocabulary:
                vocabulary=vocabulary.union([item])

    #Stores categories in reverse order : {0: u'Fast Food', 1: u'Nightlife', 2: u'Restaurants'}
    rlookup = dict(zip(range(len(vocabulary)),list(vocabulary)))
    #Stores categories in natural order : {u'Restaurants': 2, u'Nightlife': 1, u'Fast Food': 0}            
    lookup = dict(zip(list(vocabulary),range(len(vocabulary))))
    
    X_test=[]
    X_Y_train=[]
    X_validation=[]
    Y_test=[]
    Y_validation=[]
    for key in locationDict.keys():
        test_index=set(sample(locationDict[key],int(floor(len(locationDict[key])*0.2))))
        trn_val_index=[i for i in locationDict[key] if i not in test_index]
        validation_index=set(sample(trn_val_index,int(floor(len(trn_val_index)*0.1))))
        for index in locationDict[key]:
            if index in test_index:
                X_test.append(whole_X[index])
                Y_test.append(list(map(lambda x: lookup[x],whole_Y[index])))
            elif index in validation_index:
                X_validation.append(whole_X[index])
                Y_validation.append(list(map(lambda x: lookup[x],whole_Y[index])))
            else:
                X_Y_train.append([list(map(lambda x: lookup[x],whole_Y[index])),whole_X[index]])

    #print "Test,Train,Validation length",(len(X_test),len(X_Y_train),len(Y_test))
    
    X_train=[]
    Y_train=[]
    X_train,Y_train=reduce(lambda x,y: transform_to_tuple(x,y), expand_categories(X_Y_train))
    
    vectorizer = TfidfVectorizer(min_df=10,sublinear_tf=True,ngram_range=(1, 1))
    Xvec_train=vectorizer.fit_transform(X_train)
    Xvec_test=vectorizer.transform(X_test)
    Xvec_validation=vectorizer.transform(X_validation)
    
    gnb = MultinomialNB()

    models=[]
    models=models+fit_estimators((gnb,Xvec_train,Y_train))

    log_probabilities=[0]
    log_probabilities=predict((models[0],Xvec_validation))
        
    results=[]
    pltFscores=[]
    pltPrecision=[]
    pltRecall=[]
    pltlogProbs=[]
    for i in np.arange(-5,0,0.25):
        Y_pred=log_probability_filter(log_probabilities,i)
        vfsc,vprec,vrec=calculateFScore(Y_validation,Y_pred)
        results.append(((vfsc,vprec,vrec),i))
        pltFscores.append(vfsc)
        pltlogProbs.append(i)
        pltRecall.append(vrec)
        pltPrecision.append(vprec)

    results=sorted(results,key=lambda x: x[0][0],reverse=True)
    
    #Create and train the Support Vector Machine.
    svm = SVC(C=1000000.0, gamma=0.0, kernel='rbf')
    svm.fit(Xvec_train, Y_train)
    pred = svm.predict(Xvec_test)
    
    print("----------------")
    print("Test Results")
    log_probabilities=[0]

    log_probabilities=predict((models[0],Xvec_test))
    Y_pred=log_probability_filter(log_probabilities,results[0][1])
    print("Test F-Score, Accuracy",calculateFScore(Y_test,Y_pred), calculate_accuracy([row[-1] for row in Y_test],Y_pred))    
  
load_training_data()
