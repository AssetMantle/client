#!/bin/bash
cd ./app/keplr
npm i
npm install browserify
browserify main.js >> signer.js