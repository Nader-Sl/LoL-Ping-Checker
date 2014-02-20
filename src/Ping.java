/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
@author Nader Sl
**/
public class Ping {

        
        protected final String ip;
        protected long ping;

        public Ping(String ip) {
            this.ip = ip;
        }

        public String getIp() {
            return ip;
        }

        public long getPing() {
            return ping;
        }

        public void setPing(long ping) {
            this.ping = ping;
        }
        
    }

