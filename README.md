# Sapelli-Viewer
Sapelli Viewer is a client app to visualise [Sapelli](http://www.sapelli.org/) data on an offline map. It is work in progress and will synchronise data from [GeoKey](http://geokey.org.uk/) and store in on a local database. An offline mappping framework will be used to visualise stored data in the field without the need of an internet conection. 

The vision:
Remote API calls are implemented using [Retrofit](https://github.com/square/retrofit) and [RxJava 2](https://github.com/ReactiveX/RxAndroid). Data is stored using Google's [Room Persistence Library](https://developer.android.com/topic/libraries/architecture/room.html). ESRI's [ArcGIS Android SDK](https://developers.arcgis.com/android/) will be used as a mapping framework to serve locally stored map tiles. 

### About
This software is developed as part of an ERC funded project called [ECSAnVis](http://www.ucl.ac.uk/excites/projects/excites-projects/ECSAnVis/index) by the [Extreme Citizen Science (ExCiteS)](http://www.ucl.ac.uk/excites) research group at University College London. Our aim is to enable people with no or limited literacy – in the strict and broader technological sense – to use smartphones and tablets to collect, share, and analyse (spatial) data. Sapelli is used in a variety of projects related to environmental monitoring. It enables communities, regardless of social and geographical background, to map their environment and any threats it faces. Find out more about our research projects here.

If you want to get involved, chat about the project or have any other queries, please do get in touch via julia.altenbuchner@ucl.ac.uk

### Legal disclaimer
Licensed under the **Apache License, Version 2.0** (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
