/*
	Copyright (c) 2004-2007, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/book/dojo-book-0-9/introduction/licensing
*/


if(!dojo._hasResource["dojo._base.NodeList"]){dojo._hasResource["dojo._base.NodeList"]=true;dojo.provide("dojo._base.NodeList");dojo.require("dojo._base.lang");dojo.require("dojo._base.array");(function(){var d=dojo;var _2=function(_3){_3.constructor=dojo.NodeList;dojo._mixin(_3,dojo.NodeList.prototype);return _3;};dojo.NodeList=function(){return _2(Array.apply(null,arguments));};dojo.NodeList._wrap=_2;dojo.extend(dojo.NodeList,{slice:function(){var a=dojo._toArray(arguments);return _2(a.slice.apply(this,a));},splice:function(){var a=dojo._toArray(arguments);return _2(a.splice.apply(this,a));},concat:function(){var a=dojo._toArray(arguments,0,[this]);return _2(a.concat.apply([],a));},indexOf:function(_7,_8){return d.indexOf(this,_7,_8);},lastIndexOf:function(){return d.lastIndexOf.apply(d,d._toArray(arguments,0,[this]));},every:function(_9,_a){return d.every(this,_9,_a);},some:function(_b,_c){return d.some(this,_b,_c);},map:function(_d,_e){return d.map(this,_d,_e,d.NodeList);},forEach:function(_f,_10){d.forEach(this,_f,_10);return this;},coords:function(){return d.map(this,d.coords);},style:function(){var aa=d._toArray(arguments,0,[null]);var s=this.map(function(i){aa[0]=i;return d.style.apply(d,aa);});return (arguments.length>1)?this:s;},styles:function(){d.deprecated("NodeList.styles","use NodeList.style instead","1.1");return this.style.apply(this,arguments);},addClass:function(_14){this.forEach(function(i){d.addClass(i,_14);});return this;},removeClass:function(_16){this.forEach(function(i){d.removeClass(i,_16);});return this;},place:function(_18,_19){var _1a=d.query(_18)[0];_19=_19||"last";for(var x=0;x<this.length;x++){d.place(this[x],_1a,_19);}return this;},connect:function(_1c,_1d,_1e){this.forEach(function(_1f){d.connect(_1f,_1c,_1d,_1e);});return this;},orphan:function(_20){var _21=(_20)?d._filterQueryResult(this,_20):this;_21.forEach(function(_22){if(_22["parentNode"]){_22.parentNode.removeChild(_22);}});return _21;},adopt:function(_23,_24){var _25=this[0];return d.query(_23).forEach(function(ai){d.place(ai,_25,(_24||"last"));});},query:function(_27){_27=_27||"";var ret=d.NodeList();this.forEach(function(_29){d.query(_27,_29).forEach(function(_2a){if(typeof _2a!="undefined"){ret.push(_2a);}});});return ret;},filter:function(_2b){var _2c=this;var _a=arguments;var r=d.NodeList();var rp=function(t){if(typeof t!="undefined"){r.push(t);}};if(d.isString(_2b)){_2c=d._filterQueryResult(this,_a[0]);if(_a.length==1){return _2c;}d.forEach(d.filter(_2c,_a[1],_a[2]),rp);return r;}d.forEach(d.filter(_2c,_a[0],_a[1]),rp);return r;},addContent:function(_31,_32){var ta=d.doc.createElement("span");if(d.isString(_31)){ta.innerHTML=_31;}else{ta.appendChild(_31);}var ct=((_32=="first")||(_32=="after"))?"lastChild":"firstChild";this.forEach(function(_35){var tn=ta.cloneNode(true);while(tn[ct]){d.place(tn[ct],_35,_32);}});return this;}});d.forEach(["blur","click","keydown","keypress","keyup","mousedown","mouseenter","mouseleave","mousemove","mouseout","mouseover","mouseup"],function(evt){var _oe="on"+evt;dojo.NodeList.prototype[_oe]=function(a,b){return this.connect(_oe,a,b);};});})();}