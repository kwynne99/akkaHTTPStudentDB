#!/bin/zsh

echo 'Building student database...'

curl -XPOST -H "Content-type: application/json" -d '{"name":"andrew", "emplID":1, "status":"good", "GPA":"3.4", "Major":"Mathematics"}' 'http://localhost:8080/students'
echo
curl -XPOST -H "Content-type: application/json" -d '{"name":"bhiravi", "emplID":2, "status":"good", "GPA":"3.6", "Major":"History"}' 'http://localhost:8080/students'
echo
curl -XPOST -H "Content-type: application/json" -d '{"name":"carlos", "emplID":3, "status":"good", "GPA":"4.0", "Major":"Linguistics"}' 'http://localhost:8080/students'
echo
echo

echo '---- CURRENT STUDENT DATABASE ----'
curl -XGET 'http://localhost:8080/students' | json_pp
echo

echo 'Removing student: Andrew'
curl -XDELETE 'http://localhost:8080/students/andrew'
echo
echo

echo 'Looking up student: Andrew...'
curl -XGET 'http://localhost:8080/students/andrew'
echo
echo

echo 'Looking up student: Bhiravi...'
curl -XGET 'http://localhost:8080/students/bhiravi'
echo
echo

