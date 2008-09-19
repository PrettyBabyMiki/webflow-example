/*
	Copyright (c) 2004-2007, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/book/dojo-book-0-9/introduction/licensing
*/


if(!dojo._hasResource["dojo.parser"]){dojo._hasResource["dojo.parser"]=true;dojo.provide("dojo.parser");dojo.require("dojo.date.stamp");dojo.parser=new function(){var d=dojo;function val2type(_2){if(d.isString(_2)){return "string";}if(typeof _2=="number"){return "number";}if(typeof _2=="boolean"){return "boolean";}if(d.isFunction(_2)){return "function";}if(d.isArray(_2)){return "array";}if(_2 instanceof Date){return "date";}if(_2 instanceof d._Url){return "url";}return "object";};function str2obj(_3,_4){switch(_4){case "string":return _3;case "number":return _3.length?Number(_3):NaN;case "boolean":return typeof _3=="boolean"?_3:!(_3.toLowerCase()=="false");case "function":if(d.isFunction(_3)){_3=_3.toString();_3=d.trim(_3.substring(_3.indexOf("{")+1,_3.length-1));}try{if(_3.search(/[^\w\.]+/i)!=-1){_3=d.parser._nameAnonFunc(new Function(_3),this);}return d.getObject(_3,false);}catch(e){return new Function();}case "array":return _3.split(/\s*,\s*/);case "date":switch(_3){case "":return new Date("");case "now":return new Date();default:return d.date.stamp.fromISOString(_3);}case "url":return d.baseUrl+_3;default:return d.fromJson(_3);}};var _5={};function getClassInfo(_6){if(!_5[_6]){var _7=d.getObject(_6);if(!d.isFunction(_7)){throw new Error("Could not load class '"+_6+"'. Did you spell the name correctly and use a full path, like 'dijit.form.Button'?");}var _8=_7.prototype;var _9={};for(var _a in _8){if(_a.charAt(0)=="_"){continue;}var _b=_8[_a];_9[_a]=val2type(_b);}_5[_6]={cls:_7,params:_9};}return _5[_6];};this._functionFromScript=function(_c){var _d="";var _e="";var _f=_c.getAttribute("args");if(_f){d.forEach(_f.split(/\s*,\s*/),function(_10,idx){_d+="var "+_10+" = arguments["+idx+"]; ";});}var _12=_c.getAttribute("with");if(_12&&_12.length){d.forEach(_12.split(/\s*,\s*/),function(_13){_d+="with("+_13+"){";_e+="}";});}return new Function(_d+_c.innerHTML+_e);};this.instantiate=function(_14){var _15=[];d.forEach(_14,function(_16){if(!_16){return;}var _17=_16.getAttribute("dojoType");if((!_17)||(!_17.length)){return;}var _18=getClassInfo(_17);var _19=_18.cls;var ps=_19._noScript||_19.prototype._noScript;var _1b={};var _1c=_16.attributes;for(var _1d in _18.params){var _1e=_1c.getNamedItem(_1d);if(!_1e||(!_1e.specified&&(!dojo.isIE||_1d.toLowerCase()!="value"))){continue;}var _1f=_1e.value;switch(_1d){case "class":_1f=_16.className;break;case "style":_1f=_16.style&&_16.style.cssText;}var _20=_18.params[_1d];_1b[_1d]=str2obj(_1f,_20);}if(!ps){var _21=[],_22=[];d.query("> script[type^='dojo/']",_16).orphan().forEach(function(_23){var _24=_23.getAttribute("event"),_17=_23.getAttribute("type"),nf=d.parser._functionFromScript(_23);if(_24){if(_17=="dojo/connect"){_21.push({event:_24,func:nf});}else{_1b[_24]=nf;}}else{_22.push(nf);}});}var _26=_19["markupFactory"];if(!_26&&_19["prototype"]){_26=_19.prototype["markupFactory"];}var _27=_26?_26(_1b,_16,_19):new _19(_1b,_16);_15.push(_27);var _28=_16.getAttribute("jsId");if(_28){d.setObject(_28,_27);}if(!ps){dojo.forEach(_21,function(_29){dojo.connect(_27,_29.event,null,_29.func);});dojo.forEach(_22,function(_2a){_2a.call(_27);});}});d.forEach(_15,function(_2b){if(_2b&&(_2b.startup)&&((!_2b.getParent)||(!_2b.getParent()))){_2b.startup();}});return _15;};this.parse=function(_2c){var _2d=d.query("[dojoType]",_2c);var _2e=this.instantiate(_2d);return _2e;};}();(function(){var _2f=function(){if(djConfig["parseOnLoad"]==true){dojo.parser.parse();}};if(dojo.exists("dijit.wai.onload")&&(dijit.wai.onload===dojo._loaders[0])){dojo._loaders.splice(1,0,_2f);}else{dojo._loaders.unshift(_2f);}})();dojo.parser._anonCtr=0;dojo.parser._anon={};dojo.parser._nameAnonFunc=function(_30,_31){var jpn="$joinpoint";var nso=(_31||dojo.parser._anon);if(dojo.isIE){var cn=_30["__dojoNameCache"];if(cn&&nso[cn]===_30){return _30["__dojoNameCache"];}}var ret="__"+dojo.parser._anonCtr++;while(typeof nso[ret]!="undefined"){ret="__"+dojo.parser._anonCtr++;}nso[ret]=_30;return ret;};}