# administration

Supporting backend for [administration-frontend](http://github.com/cjww-development/administration-frontend)

# How to run
```sbtshell
sbt run
```

This will start the application on port **9876**

# Running tests
```sbtshell
sbt compile coverage test test it:test coverageReport
```

You can set an alias for this in **.bashrc** (ubuntu) or **.bash_profile** (mac)

# Routes
|                    Path              | Supported Methods |                                   Description                                |
| -------------------------------------| ------------------| ---------------------------------------------------------------------------- |
|  /administration/register            |        POST       | Registers a user in adminstration                                            |
|  /administration/authenticate        |        POST       | Validates that the user attempting to login, exists                          |
|  /administration/user/:managementId  |        GET        | Retrieves an admin users id, user name and email based on their managementId |
|  /administration/user/:managementId  |        DELETE     | Deletes the management user given the specified management id                |
|  /administration/users               |        GET        | Gets all management users                                                    |

# Routes breakdown

#### POST /administration/register

##### Headers
|       key        |       value      |
|------------------|------------------|
|   Content-type   | application/json | 
|   cjww-headers   | rTxo4DYUjuc-PJKeysvWddy_NO02WBQxTt4nwtDYyIzHKc4DJqR7-zilP3Ix7WEY8a4KLp78si_TnEEIn2kfSiTQh1mtof9DeKqnYmheQnfDve2iW2GgH585LsBd0IcOO2J8XAUnj1Al8I3PiK9xsg |

##### Request body
```json
{
    "username" : "testUserName",
    "email" : "testEmail",
    "password" : "testPassword"
}
```

This is the json structure needed to create a new user. The password field should be first encrypted using `com.cjwwdev.security.encryption.SHA512` and the whole body should then be encrypted using `com.cjwwdev.security.encryption.DataSecurity`.

Both of these packages can be found in [application-utilities](http://github.com/cjww-development/application-utilities).

##### Responses
**Created (201)**: The new user has passed validation and has been created.
```json
{
    "uri": "/administration/register",
    "method": "POST",
    "status": 201,
    "body": "Account created",
    "stats": {
        "requestCompletedAt": "2018-05-01T15:37:21.426"
    }
}
```


**Internal server error (500)**: There was a problem creating the user
```json
{
    "uri": "/administration/register",
    "method": "POST",
    "status": 500,
    "errorMessage": "There was a problem creating the new account",
    "stats": {
        "requestCompletedAt": "2018-05-01T15:37:21.426"
    }
}
```

#### POST /administration/authenticate

##### Headers
|       key        |       value      |
|------------------|------------------|
|   Content-type   | application/json | 
|   cjww-headers   | rTxo4DYUjuc-PJKeysvWddy_NO02WBQxTt4nwtDYyIzHKc4DJqR7-zilP3Ix7WEY8a4KLp78si_TnEEIn2kfSiTQh1mtof9DeKqnYmheQnfDve2iW2GgH585LsBd0IcOO2J8XAUnj1Al8I3PiK9xsg |

##### Request body
```json
{
    "username" : "testUserName",
    "password" : "testPassword"
}
```

This is the json structure needed to validate a user. The password field should be first encrypted using `com.cjwwdev.security.encryption.SHA512` and the whole body should then be encrypted using `com.cjwwdev.security.encryption.DataSecurity`.

Both of these packages can be found in [application-utilities](http://github.com/cjww-development/application-utilities).

##### Responses
**Ok (200)**: The new user has passed validation.
```json
{
    "uri": "/administration/authenticate",
    "method": "POST",
    "status": 201,
    "body": "<MANAGEMENT_ID>",
    "stats": {
        "requestCompletedAt": "2018-05-01T15:37:21.426"
    }
}
```

`body` contains the management id; has to be first decrypted using `com.cjwwdev.security.encryption.DataSecurity`.


**Forbidden (403)**: The given user could not be validated
```json
{
    "uri": "/administration/authenticate",
    "method": "POST",
    "status": 403,
    "errorMessage": "User could not be authenticated",
    "stats": {
        "requestCompletedAt": "2018-05-01T15:37:21.426"
    }
}
```

#### GET /administration/user/:managementId

##### Headers
|       key        |       value      |
|------------------|------------------|
|   Content-type   | text/plain | 
|   cjww-headers   | rTxo4DYUjuc-PJKeysvWddy_NO02WBQxTt4nwtDYyIzHKc4DJqR7-zilP3Ix7WEY8a4KLp78si_TnEEIn2kfSiTQh1mtof9DeKqnYmheQnfDve2iW2GgH585LsBd0IcOO2J8XAUnj1Al8I3PiK9xsg |

##### Responses
**Ok (200)**: The specified management id was matched and the account details have been returned
```json
{
    "uri": "/administration/user/:managementId",
    "method": "GET",
    "status": 200,
    "body": "<ACC_INFO>",
    "stats": {
        "requestCompletedAt": "2018-05-01T15:37:21.426"
    }
}
```

`body` contains the account details (id, username, email and permissions); has to be first decrypted using `com.cjwwdev.security.encryption.DataSecurity`.


**Not found (404)**: The specified management id could not be matched to a user
```json
{
    "uri": "/administration/user/:managementId",
    "method": "POST",
    "status": 404,
    "errorMessage": "No account found",
    "stats": {
        "requestCompletedAt": "2018-05-01T15:37:21.426"
    }
}
```

#### DELETE /administration/user/:managementId

##### Headers
|       key        |       value      |
|------------------|------------------|
|   Content-type   | text/plain | 
|   cjww-headers   | rTxo4DYUjuc-PJKeysvWddy_NO02WBQxTt4nwtDYyIzHKc4DJqR7-zilP3Ix7WEY8a4KLp78si_TnEEIn2kfSiTQh1mtof9DeKqnYmheQnfDve2iW2GgH585LsBd0IcOO2J8XAUnj1Al8I3PiK9xsg |

##### Responses
**No Content (204)**: The specified management id was deleted

**Internal Server Error (500)**: There was a problem deleting the management user
```json
{
    "uri": "/administration/user/:managementId",
    "method": "POST",
    "status": 404,
    "errorMessage": "There was a problem deleting the management user",
    "stats": {
        "requestCompletedAt": "2018-05-01T15:37:21.426"
    }
}
```

#### GET /administration/users

##### Headers
|       key        |       value      |
|------------------|------------------|
|   Content-type   | text/plain | 
|   cjww-headers   | rTxo4DYUjuc-PJKeysvWddy_NO02WBQxTt4nwtDYyIzHKc4DJqR7-zilP3Ix7WEY8a4KLp78si_TnEEIn2kfSiTQh1mtof9DeKqnYmheQnfDve2iW2GgH585LsBd0IcOO2J8XAUnj1Al8I3PiK9xsg |

##### Responses
**Ok (200)**: All found management users have been returned
```json
{
    "uri": "/administration/user/:managementId",
    "method": "POST",
    "status": 404,
    "body": "<BODY>",
    "stats": {
        "requestCompletedAt": "2018-05-01T15:37:21.426"
    }
}
```

`body` contains the account details (id, username, email and permissions) of all management users; has to be first decrypted using `com.cjwwdev.security.encryption.DataSecurity`.

**No Content (204)**: No management users could be found
                  
License
=======
This code is open sourced licensed under the Apache 2.0 License