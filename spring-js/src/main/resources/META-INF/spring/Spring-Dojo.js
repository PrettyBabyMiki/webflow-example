/*
 * Copyright 2004-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
dojo.declare("Spring.DefaultEquals",null,{equals:function(_1){if(_1.declaredClass&&_1.declaredClass==this.declaredClass){return true;}else{return false;}}});dojo.declare("Spring.ElementDecoration",[Spring.AbstractElementDecoration,Spring.DefaultEquals],{constructor:function(_2){this.copyFields=new Array("name","value","type","checked","selected","readOnly","disabled","alt","maxLength","class","title");dojo.mixin(this,_2);if(this.widgetModule==""){this.widgetModule=this.widgetType;}},apply:function(){if(dijit.byId(this.elementId)){dijit.byId(this.elementId).destroyRecursive(false);}var _3=dojo.byId(this.elementId);if(!_3){console.error("Could not apply "+this.widgetType+" decoration.  Element with id '"+this.elementId+"' not found in the DOM.");}else{for(var _4 in this.copyFields){_4=this.copyFields[_4];if(!this.widgetAttrs[_4]&&_3[_4]&&(typeof _3[_4]!="number"||(typeof _3[_4]=="number"&&_3[_4]>=0))){this.widgetAttrs[_4]=_3[_4];}}if(_3["style"]&&_3["style"].cssText){this.widgetAttrs["style"]=_3["style"].cssText;}dojo.require(this.widgetModule);var _5=dojo.eval(this.widgetType);this.widget=new _5(this.widgetAttrs,_3);this.widget.startup();}return this;},validate:function(){if(!this.widget.isValid){return true;}var _6=this.widget.isValid(false);if(!_6){this.widget.state="Error";this.widget._setStateClass();}return _6;}});dojo.declare("Spring.ValidateAllDecoration",[Spring.AbstractValidateAllDecoration,Spring.DefaultEquals],{constructor:function(_7){this.originalHandler=null;this.connection=null;dojo.mixin(this,_7);},apply:function(){var _8=dojo.byId(this.elementId);this.originalHandler=_8[this.event];var _9=this;_8[this.event]=function(_a){_9.handleEvent(_a,_9);};return this;},cleanup:function(){dojo.disconnect(this.connection);},handleEvent:function(_b,_c){if(!Spring.validateAll()){dojo.stopEvent(_b);}else{if(dojo.isFunction(_c.originalHandler)){var _d=_c.originalHandler(_b);if(_d==false){dojo.stopEvent(_b);}}}}});dojo.declare("Spring.AjaxEventDecoration",[Spring.AbstractAjaxEventDecoration,Spring.DefaultEquals],{constructor:function(_e){this.connection=null;dojo.mixin(this,_e);},apply:function(){this.connection=dojo.connect(dojo.byId(this.elementId),this.event,this,"submit");return this;},cleanup:function(){dojo.disconnect(this.connection);},submit:function(_f){if(this.sourceId==""){this.sourceId=this.elementId;}if(this.formId==""){Spring.remoting.getLinkedResource(this.sourceId,this.params,this.popup);}else{Spring.remoting.submitForm(this.sourceId,this.formId,this.params);}dojo.stopEvent(_f);}});dojo.declare("Spring.RemotingHandler",Spring.AbstractRemotingHandler,{constructor:function(){},submitForm:function(_10,_11,_12){var _13=new Object();for(var key in _12){_13[key]=_12[key];}var _15=dojo.byId(_10);if(_15!=null){if(_15.value!=undefined&&_15.type&&("button,submit,reset").indexOf(_15.type)<0){_13[_10]=_15.value;}else{if(_15.name!=undefined){_13[_15.name]=_15.name;}else{_13[_10]=_10;}}}if(!_13["ajaxSource"]){_13["ajaxSource"]=_10;}dojo.xhrPost({content:_13,form:_11,handleAs:"text",headers:{"Accept":"text/html;type=ajax"},load:this.handleResponse,error:this.handleError});},getLinkedResource:function(_16,_17,_18){this.getResource(dojo.byId(_16).href,_17,_18);},getResource:function(_19,_1a,_1b){dojo.xhrGet({url:_19,content:_1a,handleAs:"text",headers:{"Accept":"text/html;type=ajax"},load:this.handleResponse,error:this.handleError,modal:_1b});},handleResponse:function(_1c,_1d){var _1e=_1d.xhr.getResponseHeader("Spring-Redirect-URL");var _1f=_1d.xhr.getResponseHeader("Spring-Modal-View");var _20=((dojo.isString(_1f)&&_1f.length>0)||_1d.args.modal);if(dojo.isString(_1e)&&_1e.length>0){if(_20){Spring.remoting.renderURLToModalDialog(_1e,_1d);return _1c;}else{if(_1e.indexOf("/")>=0){window.location=window.location.protocol+"//"+window.location.host+_1e;}else{var _21=window.location.protocol+"//"+window.location.host+window.location.pathname;var _22=_21.lastIndexOf("/");_21=_21.substr(0,_22+1)+_1e;if(_21==window.location){Spring.remoting.getResource(_21,_1d.args.content,false);}else{window.location=_21;}}return _1c;}}var _23="(?:<script(.|[\n|\r])*?>)((\n|\r|.)*?)(?:</script>)";var _24=[];var _25=new RegExp(_23,"img");var _26=new RegExp(_23,"im");var _27=_1c.match(_25);if(_27!=null){for(var i=0;i<_27.length;i++){var _29=(_27[i].match(_26)||["","",""])[2];_29=_29.replace(/<!--/mg,"").replace(/\/\/-->/mg,"");_24.push(_29);}}_1c=_1c.replace(_25,"");var _2a=dojo.doc.createElement("div");_2a.id="ajaxResponse";_2a.style.visibility="hidden";document.body.appendChild(_2a);var _2b=new dojo.NodeList(_2a);var _2c=_2b.addContent(_1c,"first").query("#ajaxResponse > *").orphan();_2b.orphan();if(_20){Spring.remoting.renderNodeListToModalDialog(_2c);}else{_2c.forEach(function(_2d){if(_2d.id!=null&&_2d.id!=""){var _2e=dojo.byId(_2d.id);if(!_2e){console.error("An existing DOM elment with id '"+_2d.id+"' could not be found for replacement.");}else{_2e.parentNode.replaceChild(_2d,_2e);}}});}dojo.forEach(_24,function(_2f){dojo.eval(_2f);});return _1c;},handleError:function(_30,_31){console.error("HTTP status code: ",_31.xhr.status);return _30;},renderURLToModalDialog:function(url,_33){url=url+"&"+dojo.objectToQuery(_33.args.content);Spring.remoting.getResource(url,{},true);},renderNodeListToModalDialog:function(_34){dojo.require("dijit.Dialog");var _35=new dijit.Dialog({});_35.setContent(_34);dojo.connect(_35,"hide",_35,function(){this.destroyRecursive(false);});_35.show();}});dojo.declare("Spring.CommandLinkDecoration",[Spring.AbstractCommandLinkDecoration,Spring.DefaultEquals],{constructor:function(_36){dojo.mixin(this,_36);},apply:function(){var _37=dojo.byId(this.elementId);if(!dojo.hasClass(_37,"progressiveLink")){var _38=new dojo.NodeList(_37);_38.addContent(this.linkHtml,"after").orphan("*");_37=dojo.byId(this.elementId);}_37.submitFormFromLink=this.submitFormFromLink;return this;},submitFormFromLink:function(_39,_3a,_3b){var _3c=[];var _3d=dojo.byId(_39);var _3e=document.createElement("input");_3e.name=_3a;_3e.value="submitted";_3c.push(_3e);dojo.forEach(_3b,function(_3f){var _40=document.createElement("input");_40.name=_3f.name;_40.value=_3f.value;_3c.push(_40);});dojo.forEach(_3c,function(_41){dojo.addClass(_41,"SpringLinkInput");dojo.place(_41,_3d,"last");});if((_3d.onsubmit?!_3d.onsubmit():false)||!_3d.submit()){dojo.forEach(_3c,function(_42){_3d.removeChild(_42);});}}});dojo.addOnLoad(Spring.initialize);