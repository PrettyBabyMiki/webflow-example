/*
	Copyright (c) 2004-2007, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/book/dojo-book-0-9/introduction/licensing
*/


if(!dojo._hasResource["dojo._base.html"]){dojo._hasResource["dojo._base.html"]=true;dojo.require("dojo._base.lang");dojo.provide("dojo._base.html");try{document.execCommand("BackgroundImageCache",false,true);}catch(e){}if(dojo.isIE||dojo.isOpera){dojo.byId=function(id,_2){if(dojo.isString(id)){var _d=_2||dojo.doc;var te=_d.getElementById(id);if(te&&te.attributes.id.value==id){return te;}else{var _5=_d.all[id];if(!_5){return;}if(!_5.length){return _5;}var i=0;while((te=_5[i++])){if(te.attributes.id.value==id){return te;}}}}else{return id;}};}else{dojo.byId=function(id,_8){if(dojo.isString(id)){return (_8||dojo.doc).getElementById(id);}else{return id;}};}(function(){var _9=null;dojo._destroyElement=function(_a){_a=dojo.byId(_a);try{if(!_9){_9=document.createElement("div");}_9.appendChild(_a.parentNode?_a.parentNode.removeChild(_a):_a);_9.innerHTML="";}catch(e){}};dojo.isDescendant=function(_b,_c){try{_b=dojo.byId(_b);_c=dojo.byId(_c);while(_b){if(_b===_c){return true;}_b=_b.parentNode;}}catch(e){return -1;}return false;};dojo.setSelectable=function(_d,_e){_d=dojo.byId(_d);if(dojo.isMozilla){_d.style.MozUserSelect=_e?"":"none";}else{if(dojo.isKhtml){_d.style.KhtmlUserSelect=_e?"auto":"none";}else{if(dojo.isIE){_d.unselectable=_e?"":"on";dojo.query("*",_d).forEach(function(_f){_f.unselectable=_e?"":"on";});}}}};var _10=function(_11,ref){ref.parentNode.insertBefore(_11,ref);return true;};var _13=function(_14,ref){var pn=ref.parentNode;if(ref==pn.lastChild){pn.appendChild(_14);}else{return _10(_14,ref.nextSibling);}return true;};dojo.place=function(_17,_18,_19){if(!_17||!_18||_19===undefined){return false;}_17=dojo.byId(_17);_18=dojo.byId(_18);if(typeof _19=="number"){var cn=_18.childNodes;if((_19==0&&cn.length==0)||cn.length==_19){_18.appendChild(_17);return true;}if(_19==0){return _10(_17,_18.firstChild);}return _13(_17,cn[_19-1]);}switch(_19.toLowerCase()){case "before":return _10(_17,_18);case "after":return _13(_17,_18);case "first":if(_18.firstChild){return _10(_17,_18.firstChild);}else{_18.appendChild(_17);return true;}break;default:_18.appendChild(_17);return true;}};dojo.boxModel="content-box";if(dojo.isIE){var _1b=document.compatMode;dojo.boxModel=(_1b=="BackCompat")||(_1b=="QuirksMode")||(dojo.isIE<6)?"border-box":"content-box";}var gcs,dv=document.defaultView;if(dojo.isSafari){gcs=function(_1e){var s=dv.getComputedStyle(_1e,null);if(!s&&_1e.style){_1e.style.display="";s=dv.getComputedStyle(_1e,null);}return s||{};};}else{if(dojo.isIE){gcs=function(_20){return _20.currentStyle;};}else{gcs=function(_21){return dv.getComputedStyle(_21,null);};}}dojo.getComputedStyle=gcs;if(!dojo.isIE){dojo._toPixelValue=function(_22,_23){return parseFloat(_23)||0;};}else{dojo._toPixelValue=function(_24,_25){if(!_25){return 0;}if(_25=="medium"){return 4;}if(_25.slice&&(_25.slice(-2)=="px")){return parseFloat(_25);}with(_24){var _26=style.left;var _27=runtimeStyle.left;runtimeStyle.left=currentStyle.left;try{style.left=_25;_25=style.pixelLeft;}catch(e){_25=0;}style.left=_26;runtimeStyle.left=_27;}return _25;};}dojo._getOpacity=(dojo.isIE?function(_28){try{return (_28.filters.alpha.opacity/100);}catch(e){return 1;}}:function(_29){return dojo.getComputedStyle(_29).opacity;});dojo._setOpacity=(dojo.isIE?function(_2a,_2b){if(_2b==1){_2a.style.cssText=_2a.style.cssText.replace(/FILTER:[^;]*;/i,"");if(_2a.nodeName.toLowerCase()=="tr"){dojo.query("> td",_2a).forEach(function(i){i.style.cssText=i.style.cssText.replace(/FILTER:[^;]*;/i,"");});}}else{var o="Alpha(Opacity="+(_2b*100)+")";_2a.style.filter=o;}if(_2a.nodeName.toLowerCase()=="tr"){dojo.query("> td",_2a).forEach(function(i){i.style.filter=o;});}return _2b;}:function(_2f,_30){return _2f.style.opacity=_30;});var _31={width:true,height:true,left:true,top:true};var _32=function(_33,_34,_35){_34=_34.toLowerCase();if(_31[_34]===true){return dojo._toPixelValue(_33,_35);}else{if(_31[_34]===false){return _35;}else{if(dojo.isOpera&&_34=="cssText"){}if((_34.indexOf("margin")>=0)||(_34.indexOf("padding")>=0)||(_34.indexOf("width")>=0)||(_34.indexOf("height")>=0)||(_34.indexOf("max")>=0)||(_34.indexOf("min")>=0)||(_34.indexOf("offset")>=0)){_31[_34]=true;return dojo._toPixelValue(_33,_35);}else{_31[_34]=false;return _35;}}}};dojo.style=function(_36,_37,_38){var n=dojo.byId(_36),_3a=arguments.length,op=(_37=="opacity");if(_3a==3){return op?dojo._setOpacity(n,_38):n.style[_37]=_38;}if(_3a==2&&op){return dojo._getOpacity(n);}var s=dojo.getComputedStyle(n);return (_3a==1)?s:_32(n,_37,s[_37]);};dojo._getPadExtents=function(n,_3e){var s=_3e||gcs(n),px=dojo._toPixelValue,l=px(n,s.paddingLeft),t=px(n,s.paddingTop);return {l:l,t:t,w:l+px(n,s.paddingRight),h:t+px(n,s.paddingBottom)};};dojo._getBorderExtents=function(n,_44){var ne="none",px=dojo._toPixelValue,s=_44||gcs(n),bl=(s.borderLeftStyle!=ne?px(n,s.borderLeftWidth):0),bt=(s.borderTopStyle!=ne?px(n,s.borderTopWidth):0);return {l:bl,t:bt,w:bl+(s.borderRightStyle!=ne?px(n,s.borderRightWidth):0),h:bt+(s.borderBottomStyle!=ne?px(n,s.borderBottomWidth):0)};};dojo._getPadBorderExtents=function(n,_4b){var s=_4b||gcs(n),p=dojo._getPadExtents(n,s),b=dojo._getBorderExtents(n,s);return {l:p.l+b.l,t:p.t+b.t,w:p.w+b.w,h:p.h+b.h};};dojo._getMarginExtents=function(n,_50){var s=_50||gcs(n),px=dojo._toPixelValue,l=px(n,s.marginLeft),t=px(n,s.marginTop),r=px(n,s.marginRight),b=px(n,s.marginBottom);if(dojo.isSafari&&(s.position!="absolute")){r=l;}return {l:l,t:t,w:l+r,h:t+b};};dojo._getMarginBox=function(_57,_58){var s=_58||gcs(_57),me=dojo._getMarginExtents(_57,s);var l=_57.offsetLeft-me.l,t=_57.offsetTop-me.t;if(dojo.isMoz){var sl=parseFloat(s.left),st=parseFloat(s.top);if(!isNaN(sl)&&!isNaN(st)){l=sl,t=st;}else{var p=_57.parentNode;if(p&&p.style){var pcs=gcs(p);if(pcs.overflow!="visible"){var be=dojo._getBorderExtents(p,pcs);l+=be.l,t+=be.t;}}}}else{if(dojo.isOpera){var p=_57.parentNode;if(p){var be=dojo._getBorderExtents(p);l-=be.l,t-=be.t;}}}return {l:l,t:t,w:_57.offsetWidth+me.w,h:_57.offsetHeight+me.h};};dojo._getContentBox=function(_62,_63){var s=_63||gcs(_62),pe=dojo._getPadExtents(_62,s),be=dojo._getBorderExtents(_62,s),w=_62.clientWidth,h;if(!w){w=_62.offsetWidth,h=_62.offsetHeight;}else{h=_62.clientHeight,be.w=be.h=0;}if(dojo.isOpera){pe.l+=be.l;pe.t+=be.t;}return {l:pe.l,t:pe.t,w:w-pe.w-be.w,h:h-pe.h-be.h};};dojo._getBorderBox=function(_69,_6a){var s=_6a||gcs(_69),pe=dojo._getPadExtents(_69,s),cb=dojo._getContentBox(_69,s);return {l:cb.l-pe.l,t:cb.t-pe.t,w:cb.w+pe.w,h:cb.h+pe.h};};dojo._setBox=function(_6e,l,t,w,h,u){u=u||"px";with(_6e.style){if(!isNaN(l)){left=l+u;}if(!isNaN(t)){top=t+u;}if(w>=0){width=w+u;}if(h>=0){height=h+u;}}};dojo._usesBorderBox=function(_74){var n=_74.tagName;return dojo.boxModel=="border-box"||n=="TABLE"||n=="BUTTON";};dojo._setContentSize=function(_76,_77,_78,_79){var bb=dojo._usesBorderBox(_76);if(bb){var pb=dojo._getPadBorderExtents(_76,_79);if(_77>=0){_77+=pb.w;}if(_78>=0){_78+=pb.h;}}dojo._setBox(_76,NaN,NaN,_77,_78);};dojo._setMarginBox=function(_7c,_7d,_7e,_7f,_80,_81){var s=_81||dojo.getComputedStyle(_7c);var bb=dojo._usesBorderBox(_7c),pb=bb?_85:dojo._getPadBorderExtents(_7c,s),mb=dojo._getMarginExtents(_7c,s);if(_7f>=0){_7f=Math.max(_7f-pb.w-mb.w,0);}if(_80>=0){_80=Math.max(_80-pb.h-mb.h,0);}dojo._setBox(_7c,_7d,_7e,_7f,_80);};var _85={l:0,t:0,w:0,h:0};dojo.marginBox=function(_87,box){var n=dojo.byId(_87),s=gcs(n),b=box;return !b?dojo._getMarginBox(n,s):dojo._setMarginBox(n,b.l,b.t,b.w,b.h,s);};dojo.contentBox=function(_8c,box){var n=dojo.byId(_8c),s=gcs(n),b=box;return !b?dojo._getContentBox(n,s):dojo._setContentSize(n,b.w,b.h,s);};var _91=function(_92,_93){if(!(_92=(_92||0).parentNode)){return 0;}var val,_95=0,_b=dojo.body();while(_92&&_92.style){if(gcs(_92).position=="fixed"){return 0;}val=_92[_93];if(val){_95+=val-0;if(_92==_b){break;}}_92=_92.parentNode;}return _95;};dojo._docScroll=function(){var _b=dojo.body();var _w=dojo.global;var de=dojo.doc.documentElement;return {y:(_w.pageYOffset||de.scrollTop||_b.scrollTop||0),x:(_w.pageXOffset||dojo._fixIeBiDiScrollLeft(de.scrollLeft)||_b.scrollLeft||0)};};dojo._isBodyLtr=function(){return !("_bodyLtr" in dojo)?dojo._bodyLtr=dojo.getComputedStyle(dojo.body()).direction=="ltr":dojo._bodyLtr;};dojo._getIeDocumentElementOffset=function(){var de=dojo.doc.documentElement;if(dojo.isIE>=7){return {x:de.getBoundingClientRect().left,y:de.getBoundingClientRect().top};}else{return {x:dojo._isBodyLtr()||window.parent==window?de.clientLeft:de.offsetWidth-de.clientWidth-de.clientLeft,y:de.clientTop};}};dojo._fixIeBiDiScrollLeft=function(_9b){if(dojo.isIE&&!dojo._isBodyLtr()){var de=dojo.doc.documentElement;return _9b+de.clientWidth-de.scrollWidth;}return _9b;};dojo._abs=function(_9d,_9e){var _9f=_9d.ownerDocument;var ret={x:0,y:0};var _a1=false;var db=dojo.body();if(dojo.isIE){var _a3=_9d.getBoundingClientRect();var _a4=dojo._getIeDocumentElementOffset();ret.x=_a3.left-_a4.x;ret.y=_a3.top-_a4.y;}else{if(_9f["getBoxObjectFor"]){var bo=_9f.getBoxObjectFor(_9d);ret.x=bo.x-_91(_9d,"scrollLeft");ret.y=bo.y-_91(_9d,"scrollTop");}else{if(_9d["offsetParent"]){_a1=true;var _a6;if(dojo.isSafari&&(gcs(_9d).position=="absolute")&&(_9d.parentNode==db)){_a6=db;}else{_a6=db.parentNode;}if(_9d.parentNode!=db){var nd=_9d;if(dojo.isOpera||(dojo.isSafari>=3)){nd=db;}ret.x-=_91(nd,"scrollLeft");ret.y-=_91(nd,"scrollTop");}var _a8=_9d;do{var n=_a8["offsetLeft"];if(!dojo.isOpera||n>0){ret.x+=isNaN(n)?0:n;}var m=_a8["offsetTop"];ret.y+=isNaN(m)?0:m;_a8=_a8.offsetParent;}while((_a8!=_a6)&&_a8);}else{if(_9d["x"]&&_9d["y"]){ret.x+=isNaN(_9d.x)?0:_9d.x;ret.y+=isNaN(_9d.y)?0:_9d.y;}}}}if(_a1||_9e){var _ab=dojo._docScroll();var m=_a1?(!_9e?-1:0):1;ret.y+=m*_ab.y;ret.x+=m*_ab.x;}return ret;};dojo.coords=function(_ac,_ad){var n=dojo.byId(_ac),s=gcs(n),mb=dojo._getMarginBox(n,s);var abs=dojo._abs(n,_ad);mb.x=abs.x;mb.y=abs.y;return mb;};})();dojo.hasClass=function(_b2,_b3){return ((" "+dojo.byId(_b2).className+" ").indexOf(" "+_b3+" ")>=0);};dojo.addClass=function(_b4,_b5){_b4=dojo.byId(_b4);var cls=_b4.className;if((" "+cls+" ").indexOf(" "+_b5+" ")<0){_b4.className=cls+(cls?" ":"")+_b5;}};dojo.removeClass=function(_b7,_b8){_b7=dojo.byId(_b7);var t=dojo.trim((" "+_b7.className+" ").replace(" "+_b8+" "," "));if(_b7.className!=t){_b7.className=t;}};dojo.toggleClass=function(_ba,_bb,_bc){if(_bc===undefined){_bc=!dojo.hasClass(_ba,_bb);}dojo[_bc?"addClass":"removeClass"](_ba,_bb);};}