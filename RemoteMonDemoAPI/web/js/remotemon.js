var API_KEY = "dd370eabf1fde6beeab83ec9c288e0abb4639654";
var SWARM_ID = "82bf3639e5d52500bd3384efe6e9892b42ff6c0c";
var RESOURCE_ID = "a010cd6407de897e41b2d0acfe8943dca0d8b666";

var rangedata = new Array();
var tempdata = new Array();
var humiditydata = new Array();
var dewdata = new Array();
var pressuredata = new Array();
var lightdata = new Array();
var weatherDataSets = new Array();
var startTime = 0;
var xAxisLength = 30;
var plot, weatherplot;
var plotOptions = {
        series: { shadowSize: 0 }, // drawing is faster without shadows
        grid: { color: "#FFF" },
        legend: { backgroundColor: "#5C5D60" },
        yaxes: [ { position: "left"}, { position: "right"} ]
    };
var checkedList = new Array();
var range = false;
var motion = false;

function onPresence(presence) {
   //console.log('presence -> ' + presence);
}

function onMessage(message) {
   var payload = JSON.parse(message).message.payload;
   //console.log('Got data '+JSON.stringify(payload));
   var currentTime = (new Date()).getTime();
   if (payload.protocol == "com.buglabs.xbee.protocol.SparkfunWeatherboard") {
      $.each(payload, function(key, value) {
         $('span#'+key).text(value);
         if (key == "Temperature"){
            tempdata.push([(currentTime-startTime)/1000,value]);
            if (tempdata.length > xAxisLength)
               tempdata.shift(); 
         }
         if (key == "Humidity"){
            humiditydata.push([(currentTime-startTime)/1000,value]);
            if (humiditydata.length > xAxisLength)
               humiditydata.shift(); 
         }
         if (key == "Dewpoint"){
            dewdata.push([(currentTime-startTime)/1000,value]);
            if (dewdata.length > xAxisLength)
               dewdata.shift(); 
         }
         if (key == "Pressure"){
            pressuredata.push([(currentTime-startTime)/1000,value]);
            if (pressuredata.length > xAxisLength)
               pressuredata.shift(); 
         }
         if (key == "Light"){
            lightdata.push([(currentTime-startTime)/1000,value]);
            if (lightdata.length > xAxisLength)
               lightdata.shift(); 
         }
      });
      updateWeatherPlot();
   } else if (payload.protocol == "com.buglabs.xbee.protocol.MaxbotixRangefinder") {
      if (!range){
         range = true;
         $('div#rangeplot').show();
      }
      $('span#range').text(payload.Range);
      rangedata.push([(currentTime-startTime)/1000,payload.Range]);
      if (rangedata.length > xAxisLength)
         rangedata.shift();
      //console.log(rangedata);
      plot = $.plot($('#rangeplot'),[ rangedata ], plotOptions);
   } else if (payload.protocol == "com.buglabs.xbee.protocol.PIRMotion") {
      if (!motion){
         motion = true;
         $('div#motionlog').show();
      }
      var now = new Date();
      $('div#motionlog').prepend('Motion at '+(now.getMonth()+1)+'/'+now.getDate()+'/'+
         now.getFullYear()+' '+now.getHours()+':'+((now.getMinutes()<10)?'0':'')+
         now.getMinutes()+':'+((now.getSeconds()<10)?'0':'')+now.getSeconds()+'<br />');
   }
}
function onError(error) {
   console.log('error! -> ' + JSON.stringify(error));
}
function onConnect() {
   console.log('connected');
   startTime = (new Date()).getTime();
   (new Date()).getTime();
   $('div#rangeplot').hide();
   plot = $.plot($('#rangeplot'),[ rangedata ], plotOptions);
   $('div#weatherPlot').hide();
   updateWeatherPlot();
   $('input#graphselect').on('change', function() {
      checkedList.push(this);
      if ($('input#graphselect:checked').length > 2){
         var first = checkedList.shift();
         first.checked = false;
      }
      updateWeatherAxes();      
   });
}

function updateWeatherAxes() {
   weatherDataSets = new Array();
   var idx = 1;
   if ($('input#graphselect:checked').length == 0)
      $('div#weatherPlot').hide();
   else
      $('div#weatherPlot').show();
   $.each($('input#graphselect:checked'),function () {
      if (this.value == "temp"){
         weatherDataSets.push({label:"temperature",data:tempdata,yaxis:idx++});
      } else if (this.value == "humidity"){
         weatherDataSets.push({label:"humidity",data:humiditydata,yaxis:idx++});
      } else if (this.value == "dewpoint"){
         weatherDataSets.push({label:"dew point",data:dewdata,yaxis:idx++});
      } else if (this.value == "pressure"){
         weatherDataSets.push({label:"pressure",data:pressuredata,yaxis:idx++});
      } else if (this.value == "light"){
         weatherDataSets.push({label:"light",data:lightdata,yaxis:idx++});
      }
   });
}

function updateWeatherPlot() {
   weatherplot = $.plot($('#weatherplot.active'),weatherDataSets, plotOptions);
}

SWARM.connect({ apikey: API_KEY, 
               resource: RESOURCE_ID, 
               swarms: [SWARM_ID], 
               onmessage: onMessage, 
               onpresence: onPresence,
               onerror: onError,
               onconnect: onConnect});
