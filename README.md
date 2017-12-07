# Usage of PostgreSQL and PreparedStatement
## MTP Lab9

**List of tables:**
- User
- Contributor
- RepositoryOwner
- Repository
- Language
- Repository_Contributor (Link table)

**List of select queries:**
- Owners of projects written in certain language or unnamed
- Contributors to more than 1 repo
- Contributors with more than N total commits
- Repos with specific textpart in description
- The most popular programming language

### Results on 07.12.2017
*The selection was made for 2 months old repos. 35 most starred and 35 most commited. Each with top 40 contributers.*

**-----Owners Of Java Projects or unnamed:-----**
* thedaviddias with id: 237229
* vitalysim with id: 7724350
* RedditSota with id: 33503115
* Alibaba-Technology with id: 33146664
* Rsplwe with id: 25134114
* hacktoberfest17 with id: 32809007
* AlexanderBartash with id: 932153
* kristate with id: 44620
* karlhorky with id: 1935696

**-----Contributors to more than 1 repo:-----**
* WittBulter with id: 11304944
* cdubz with id: 10456740
* thedaviddias with id: 237229
* tmm1 with id: 2567
* xxhomey19 with id: 1211322 
* clickthisnick with id: 7855189

**-----Contributors with more than 1300 total commits:-----**
* jmalinen with id: 5983923
* antirez with id: 65632
* juhosg with id: 19352056
* ansgarbecker with id: 7986591
* ffainelli with id: 1110044
* jow- with id: 2528802
* psycho-nico with id: 170630
* phuslu with id: 195836
* hauke with id: 78494
* kaloz with id: 7490166

**-----Repos with 'tool' in description-----**
* owner: airbnb  
name: Lona  
description: A tool for defining design systems and using them to generate cross-platform UI code, Sketch files, images, and other artifacts.  
language: Swift  
starsCount: 3402  

* owner: Microsoft   
name: sqlopsstudio  
description: SQL Operations Studio is a data management tool that enables working with SQL Server, Azure SQL DB and SQL DW from Windows, macOS and Linux.  
language: TypeScript  
starsCount: 2532  

* owner: reactopt  
name: reactopt  
description: A CLI React performance optimization tool that identifies potential unnecessary re-rendering  
language: JavaScript  
starsCount: 1619  

* owner: vuetwo  
name: vuetron  
description: A tool for testing and debugging your Vue + Vuex applications. 是一個可以幫助您 Vue.js 的項目測試及偵錯的工具, 也同時支持 Vuex及 Vue-Router.  
language: Vue  
commitsCount: 354  

**-----The most popular language:-----**
* JavaScript
