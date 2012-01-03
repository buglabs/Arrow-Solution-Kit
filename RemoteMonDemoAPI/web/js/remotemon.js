var API_KEY = "dd370eabf1fde6beeab83ec9c288e0abb4639654";
var SWARM_ID = "82bf3639e5d52500bd3384efe6e9892b42ff6c0c";
var RESOURCE_ID = "a010cd6407de897e41b2d0acfe8943dca0d8b666";

function onPresence(presence) {
   //console.log('presence -> ' + presence);
}

function onMessage(message) {
   var payload = JSON.parse(message).message.payload;
   //console.log('Got data '+JSON.stringify(payload));
   if (payload.protocol == "com.buglabs.xbee.protocol.SparkfunWeatherboard") {
      $.each(payload, function(key, value) {
         $('span#'+key).text(value);
      });
   } else if (payload.protocol == "com.buglabs.xbee.protocol.MaxbotixRangefinder") {
      $('span#range').text(payload.Range);
   } else if (payload.protocol == "com.buglabs.xbee.protocol.PIRMotion") {
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
}
SWARM.connect({ apikey: API_KEY, 
               resource: RESOURCE_ID, 
               swarms: [SWARM_ID], 
               onmessage: onMessage, 
               onpresence: onPresence,
               onerror: onError,
               onconnect: onConnect});
