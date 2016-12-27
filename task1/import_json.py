'''
    Author: Jay Nagle (jaynagle)
'''
import json 
import sys
import os
from json import JSONDecoder
import functools
from pymongo import MongoClient

client = MongoClient('localhost', 27017)
db = client.yelp

business_list = {}
def json_parse(fileobj, decoder=json.JSONDecoder(), buffersize=2048, delimiters=None):
    remainder = ''
    for chunk in iter(functools.partial(fileobj.read, buffersize), ''):
        remainder += chunk
        while remainder:
            try:
                stripped = remainder.strip(delimiters)
                result, index = decoder.raw_decode(stripped)
                yield result
                remainder = stripped[index:]
            except ValueError:
                break

def load_businesses():
    with open('../data/yelp_academic_dataset_business.json', 'r') as file:
        for data in json_parse(file):
            business_list[data["business_id"]] = data
    
    db.business.insert_many([d for d in business_list.values()])
    db.business.create_index('business_id')

def load_reviews():
    review_list = []
    count = 0
    with open('../data/yelp_academic_dataset_review.json', 'r') as file:
        for data in json_parse(file):
            if count < 10000:
                review_list.append([data["review_id"], data["text"], business_list[data["business_id"]]["categories"]])
                count += 1
            else:
                db.reviews.insert_many([{'review_id': review_id, 'text': text, 'categories': cat} for review_id, text, cat in review_list])
                review_list = []
                count = 0         
                
def load_tips():
    tip_list = []
    count = 0
    with open('../data/yelp_academic_dataset_tip.json', 'r') as file:
        for data in json_parse(file):
            if count < 10000:
                tip_list.append([data["tip_id"], data["text"], business_list[data["business_id"]]["categories"]])
                count += 1
            else:
                db.tips.insert_many([{'tip_id': review_id, 'text': text, 'categories': cat} for tip_id, text, cat in tip_list])
                tip_list = []
                count = 0         
load_businesses()
load_reviews()
load_tips()