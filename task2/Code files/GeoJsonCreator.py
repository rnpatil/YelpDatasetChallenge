__author__ = 'Rohit Patil'

import json

output_file = open('C:\Users\Rohit\Desktop\search_task2\yelp_dataset_challenge_academic_dataset\yelp_geospatial_business.json','w')
with open('C:\Users\Rohit\Desktop\search_task2\yelp_dataset_challenge_academic_dataset\yelp_academic_dataset_business.json') as file:
    for line in file:
        #print line
        jsonData=json.loads(line)
        geojson = {"business_id" : jsonData["business_id"], "name":jsonData["name"],"stars":jsonData["stars"], "review_count":jsonData["review_count"], "categories":jsonData["categories"], "location" : {"type": "Point","coordinates": [jsonData["longitude"], jsonData["latitude"]]} }

        json.dump(geojson,output_file)
        output_file.write('\n')

output_file.close()
