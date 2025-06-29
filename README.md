How the bot works:

It polls the api every 5 seconds, when a map changes and if the map now is something we don't want we change map according to the list passed through discord. There is only one instance where bot can't check if the map changed when a map loops to itself. For example, in the image below if brusilov changes to brusilov as a loop in this case bot fails. No biggie, no one likes brusilov anyway but this map and the rotation in the image will be different everytime you recreate operations using GameTools Network. 

![image](https://github.com/user-attachments/assets/6945e76c-8b2f-4ba8-98da-a80ce9499ecb)
(Credits to Publii_u on discord for making the chart)

Disclaimer: All the above code is made through the use of GameTools Network API which is available for everyone. Simulating blaze would be a much easier way compared to this but it requires extensive reverse engineering which is very difficult with ea anti cheat. (although a few people did it before bcz no eac)

To create a .jar file run:
mvn package clean

