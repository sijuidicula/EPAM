# ODX Graph Loader

The application should be used to load and validate data from Polaris Excel datasheet to Neo4j database and validate it.

It consists of two modules:
 - Loader
 - Validator
 
 ## Prior to start
 
 1. Local or remote Neo4j database should be up and running. Its accessibility credentials should be used in next classes: 
    - PropertyGraphUploader (Loader module)
    - Requester (Validator module)
 
 2. [***Neosemantics***](https://github.com/neo4j-labs/neosemantics/releases) plugin should be placed in 
 DB's **<neo4j-home>\plugins** folder.
 
 3. Transaction validation should be allowed in DB. Add next line to **neo4j.conf** file:
    - ***apoc.trigger.enabled=true***
    
Restart you Neo4j DB after adding plugins and changing configurations.
  
 ### Loader
 
 Module loads data to Neo4j DB by running **UploadSeparatelyWithShaclMain** class. Classes are loaded in chunks and relations 
 are loaded separately.
 
 Original Polaris Excel file should be placed in **loader/src/main/resources** folder. 
 
 To reduce RAM usage, big Excel file (size > 8 Mb) can be separated into different sheets and 
 these sheets should be placed in the same directory.
 
 ##### Naming convention to be followed:
 - If one Excel file contains all information, each sheet's name should coincide with domain class name which it represents.
 - If each file represents separate domain class, file's name and single sheet's name inside a file should coincide with
 domain class name.
 
  ### Loader
  
  Module validates graph (schema and data) by running **GraphDataTest**, **SchemaTest** and **UseCaseTest** classes.

 
 
 