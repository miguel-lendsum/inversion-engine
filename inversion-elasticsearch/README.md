Integration Environment Setup


### Elasticsearch Docker Setup

  
##### Start Elasticsearch
 - `docker network create invnetwork`  
 - `docker run -d --name invelastic --net invnetwork -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:7.6.1`
 - Docker will be available at http://localhost:9200
  
##### Stop Elasticsearch 
 - `docker container stop invelastic`  

