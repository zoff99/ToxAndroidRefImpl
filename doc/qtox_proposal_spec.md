
# TRIfA and ToxProxy qTox addons proposal
<br>

## Adding a Relay Workflow
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
               |         |           +-------~~~>> (1) send ToxProxy PubKey to qTox friend
               |         +-----------+  |              CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND  
               +------------------------+





```

<br>


```
          +-------------------+                          
          |                   |                          
          |    qTox           <----------------~~~ (1) receive Friends ToxProxy Pubkey                           
          |                   |                        CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND  
          +-------------------+                          


                   (2) add Friends ToxProxy Pubkey and mark as Relay
                       show some UI to get user consent, before adding

                   (?) When deleting a Friend, also delete it's Relay



```

<br>



## sending a Message Workflow
<br>
<br>

```
          +-------------------+                          
          |                   |                          
          |    qTox           +-------~~~>> (1) send CONTROL_PROXY_MESSAGE_TYPE_WAKEUP_REQUEST                          
          |                   |                 to Friends Relay
          +---------+---------+                 (if friend has Relay, and Relay is online)         
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
          |    qTox           +-------~~~>> (1) send CONTROL_PROXY_MESSAGE_TYPE_WAKEUP_REQUEST                          
          |                   |                 to Friends Relay
          +---------+---------+                 (if friend has Relay, and Relay is online)         
                    |
                    |
                    |
                    v

                   (2) if Friend is online, call normally

                   (2a) if Friend is not yet online,
                        show an intermediary call waiting screen.
                        when friend comes online proceed to calling normally.



```




## New packets


#### CONTROL_PROXY_MESSAGE_TYPE_PROXY_PUBKEY_FOR_FRIEND

|what       |Length               | Contents
|:----------|:--------------------|:-------------
|pkt id     | 1                   | `uint8_t` 176
|pubkey     | TOX_PUBLIC_KEY_SIZE | `*uint8_t` ToxProxy pubkey to send to a friend


#### CONTROL_PROXY_MESSAGE_TYPE_WAKEUP_REQUEST

|what       |Length               | Contents
|:----------|:--------------------|:-------------
|pkt id     | 1                   | `uint8_t` 180




