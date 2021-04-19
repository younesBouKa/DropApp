import api from './Api';
"use strict";
class Logger {

    // Log levels as per https://tools.ietf.org/html/rfc5424
    static get ERROR()  { return 3; }
    static get WARN()   { return 4; }
    static get INFO()   { return 6; }
    static get DEBUG()  { return 7; }

    static get DEFAULT_BATCH_SIZE() {return 100;}

    constructor(options) {

        if ( !options || typeof options !== 'object' ) {
            throw new Error('options are required, and must be an object');
        }

        if (!options.url) {
            throw new Error('options must include a url property');
        }

        this.url         =   options.url;
        this.headers     =   options.headers;
        this.level       =   options.level || Logger.ERROR;
        this.batch_size =   options.batch_size || Logger.DEFAULT_BATCH_SIZE;
        this.messages   =   [];

    }

    send(messages) {
        let data = {
            context   :   navigator.userAgent,
            messages  :   messages
        };
        api.postData(this.url,data,{headers: this.headers},(response)=>{
            console.log("send logs",response);
        },(error)=>{
            console.error("send logs", error);
        })
    }

    logWithLevel(level, ...message){
        if(message.length===0)
            return;
        message = message.map(item=>{
            if(typeof item ==="object"){
                try{
                    item = JSON.stringify(item);
                }catch (e) {
                }
            }
            return item;
        })
        this.messages.push({
            level : level,
            message : message
        });
        if (this.messages.length >= this.batch_size) {
            this.send(this.messages.splice(0, this.batch_size));
        }
    }

    log(...message) {
        this.logWithLevel("DEBUG", message);
    }

    error(...message) {
        this.logWithLevel("ERROR", message);
    }

    warn(...message) {
        this.logWithLevel("WARN", message);
    }

    info(...message) {
        this.logWithLevel("INFO", message);
    }

    debug(...message) {
        this.logWithLevel("DEBUG", message);
    }

}

export default Logger;