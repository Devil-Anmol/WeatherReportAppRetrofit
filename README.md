# Weather Report App
This app helps in finding min and max temperature of a place from its latitude and longitude and date for which you want the temperatures. This app is made in jetpack compose and uses open Weather Api of 
Open-Meteo.
## Functionalities of App
It is using Room to create Database and storing
  
  Entities in database:
  
    id: String -> latitude + longitude + date
    date: String -> YYYY-MM-DD
    max temp: Float 
    min temp: Float
    latitude: Double
    Longitude: Double

If any request is already present in the database, then it is replied from there else it is called using API request (*Retrofit*). For Api request internet is required otherwise it works without internet if data is present
in database else gives error toast message.

If user enters a date of future then all the data available for that latitude, longitude of past 10 years is averaged

## Drawbacks
There is a drawback of the API, it doesnt return data of today and yesterday, so it is also handled as future dates.

## Images
![WeatherApp1](https://github.com/Devil-Anmol/WeatherReportApp/assets/108612802/575736eb-843d-4d6d-8bb8-a6a566d9a406)

![WeatherApp2](https://github.com/Devil-Anmol/WeatherReportApp/assets/108612802/398677ba-b8a5-4249-98cc-e272310e3a07)
