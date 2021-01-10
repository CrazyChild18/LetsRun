# LetsRun

## Introduction
This is an entry-level Android project designed for users to track their runs and query their personal exercise data.At the same time, a friend function was added, which allows you to add friends and view your friends' sports data

## Technology
- Baidu map API is used to achieve positioning and track recording functions
- Use BMob cloud database technology to realize personal information storage and friend list functions

## Using information
This project is a test project, so you need to replace your personal key and Bmobkey in order to synchronize your own data 

Uploading avatars is limited to the cloud server, so it may not be possible to upload avatars after the server expires 

## Issue
1. Want to increase in the home page view friends location function, and friends to achieve location sharing
2. If there is no view of the details after the movement in the movement record, there is empty movement data in the database, which will cause the failure to calculate the movement distance and the page crash
3. Sometimes there is a page exit need to repeat exit situation, should be the Activity jump problem
