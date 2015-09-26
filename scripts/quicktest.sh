#!/bin/bash
        for i in `seq 1 10`;
        do
		curl -H "Content-Type: application/json" -X POST -d '{"regNumber": "NWP'$i'T"}' http://localhost:8080/api/vehicles
        done   
