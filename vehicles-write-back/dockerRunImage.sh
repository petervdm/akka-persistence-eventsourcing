#!/usr/bin/env bash
docker run --name vehicles-write-back -d -p 9100:9100 -p 9101:9101 architecture/vehicles-write-back
