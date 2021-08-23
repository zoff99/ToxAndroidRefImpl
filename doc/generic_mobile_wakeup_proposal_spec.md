
# GEMP - GEneric Mobile Push (proposal)
<br>

## Adding a Push Token URL Workflow
<br>
<br>


```


                     Android Device                                
               +------------------------+       
               |                        |                          
               |                        |     
               |                        |     
               |         +-----------+  |     
               |         |TRIfA      |  |    
               |         |           |  |  
               |         |           |  |
               |         |           +-------~~~>> (1) send Push URL to qTox friend
               |         +-----------+  |              CONTROL_PROXY_MESSAGE_TYPE_PUSH_URL_FOR_FRIEND  
               +------------------------+





```

<br>


```
          +-------------------+                          
          |                   |                          
          |    qTox           <----------------~~~ (1) receive Friends Push URL                           
          |                   |                        CONTROL_PROXY_MESSAGE_TYPE_PUSH_URL_FOR_FRIEND  
          +-------------------+                          


                   (2a) add Friends Push URL (if not yet added)
                       (show some UI to get user consent, before adding)

                   (2b) change Friends Push URL (if already added and different)
                       (show some UI to get user consent, before changing)

                   (2c) delete Friends Push URL when Push URL sent is "" (if already added)
                       (show some UI to get user consent, before deleting)

                   (?) When deleting a Friend, also delete it's Push URL



```

<br>



## sending a Message Workflow
<br>
<br>

```
          +-------------------+                          
          |                   |                          
          |    qTox           +-------~~~>> (1) call Push URL (with any POST "dummy" param)                          
          |                   |                 if Friend is not online
          +---------+---------+                 (only if friend has Push URL set)                                                     
                    |
                    |
                    |
                    v

                   (2) send Message normally to Friend
                       (if Friend is not yet online, faux offline Messaging
                        will send the Message after Friend has woken up)


```


<br>

## calling a Friend Workflow
<br>
<br>

```
          +-------------------+                          
          |                   |                          
          |    qTox           +-------~~~>> (1) call Push URL (with any POST "dummy" param)                         
          |                   |                 if Friend is not online   
          +---------+---------+                 (only if friend has Push URL set)         
                    |
                    |
                    |
                    v

                   (2) if Friend is online, call normally

                   (2a) if Friend is not yet online,
                        show an intermediary call waiting screen.
                        when friend comes online proceed to calling automatically.
                        (add button to cancel the intermediary call waiting screen)



```




## New packets


#### CONTROL_PROXY_MESSAGE_TYPE_PUSH_URL_FOR_FRIEND

|what       |Length               | Contents
|:----------|:--------------------|:-------------
|pkt id     | 1                   | `uint8_t` 181
|Push URL   | [0, 1000]           | `*uint8_t` Push Token (an HTTPS: URL)


## Sanity checks on Push URL

* check that URL starts with "https://"
* allow URL to be empty, to indicate "removal" or Push URL
* show a Dialog to qTox user with information what this is used for and clearly show the URL within the Dialog

