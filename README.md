# Landmark Remark

## Outline
This app is primarily focused on looking at notes about lardmarks. As such I wanted to integrate the
google places API such that most notes would have a logical connection to a landmark. My implementation
still allows for unknown locations using geo coordinates however. I attempted to have a database that
was primarily de-normalised that allows rapid response for geo coordinates while also allowing list of
remarks per user and per landmark available. This was another implicit requirement as this database
 could grow quite large. 
 ## Time allocation
 * UI 4 hours (Google Maps, other UI elements)
 * Location 3 hours (Places Picker, Google Location API)
 * Database 3 hours (Firebase Database)
 * Search 2 hours (Algolia Search index receiving updates from Firebase Database via a Node.js 
 script running on Heroku)
 ##
 Unfortunately I wasn't able to finish the search implementation. After a few trial and error with
 some other implementations and finally settling on Algolia I ran out of time to implement the UI
 and display results. The app also load all pins currently on the map, this would be an easy fix
 with a bit more work on setting the filters for the database call. The my places button has also
 not been implemented but the database is setup to make this data retrieval simple.