import java.util.*;


/*
enum msgType {
        CALL  = 0,
        REPLY = 1
};

struct rpcMsg {
    unsigned int xid;       // unsigned
    union switch (msg_type mtype) {
        case CALL:
            call_body cbody;
        case REPLY:
            reply_body rbody;
        } body;
};
*/

enum MsgType {
    CALL,
    REPLY
}

class RpcMsg {
    int xid;            // unsigned
    MsgType mType;

    RpcMsg(int xid, MsgType mType){
        this.xid = xid;
        this.mType = mType;
    }   

    void incrementXid(){
        xid++;
    }
}

class CallBody {
    int rpcVersion;
    int program;
    int pVersion;
    int procedure;

    CallBody(int rpcVersion, int program, int pVersion, int procedure){
        this.rpcVersion = rpcVersion;
        this.program = program;
        this.pVersion = pVersion;
        this.procedure = procedure;
    }

}

enum ReplyStat{
    MSG_ACCEPTED,
    MSG_DENIED
}

class ReplyBody {
    ReplyStat stat;

    ReplyBody(ReplyStat stat){
        this.stat = stat;
    }
}

enum AcceptStat {
    SUCCESS,        /* RPC executed successfully       */
    PROG_UNAVAIL,   /* remote hasn't exported program  */
    PROG_MISMATCH,  /* remote can't support version #  */
    PROC_UNAVAIL,   /* program can't support procedure */
    GARBAGE_ARGS,   /* procedure can't decode params   */
    SYSTEM_ERR       /* e.g. memory allocation failure  */
}

class AcceptedReply {
    AcceptStat acceptStat;

    AcceptedReply(AcceptStat acceptStat){
        this.acceptStat = acceptStat;
    }
}

enum RejectStat {
    RPC_MISMATCH,
    AUTH_ERROR
}

class RejectedReply {
    RejectStat rejectStat;

    RejectedReply(RejectStat rejectStat){
        this.rejectStat = rejectStat;
    }
}


class XDREncoder { 

    RpcMsg message;
    CallBody cbody;
    ReplyBody rbody;
    AcceptedReply areply;
    RejectedReply rreply;


    XDREncoder(CallBody cbody){
        this.cbody = cbody;      
    }


    byte[] encodeRpcMsg(int xid, MsgType mType) {
        
    
        switch(mType){
            
            case CALL:

                byte[] body = encodeBody(this.cbody);
                byte[] xidBytes = encodeInt(xid);
                byte[] temp = new byte[4 + body.length];

                int i = 0;
                for (byte x : xidBytes) temp[i++] = x;
                for(byte b : body) temp[i++] = b;

                System.out.println("Encoded RpcMessage: "+ Arrays.toString(temp));

                return temp; 

            case REPLY:


                switch(this.rbody.stat){
                    case MSG_ACCEPTED:

                        switch(this.areply.acceptStat){
                            case SUCCESS:        /* RPC executed successfully       */
                                
                                String success = "successs";
                                byte[] s = success.getBytes();
                                byte[] results = new byte[4 + 8];
                                byte[] xi = encodeInt(xid);

                                int k = 0;

                                for(byte b : xi) results[k++] = b;
                                for(byte c : s) s[k++] = c;

                                return results;
                            case PROG_UNAVAIL:  /* remote hasn't exported program  */
                                break;
                            case PROG_MISMATCH: /* remote can't support version #  */
                                break;
                            case PROC_UNAVAIL:   /* program can't support procedure */
                                break;
                            case GARBAGE_ARGS:   /* procedure can't decode params   */
                                break;

                        }

                        break;
                    case MSG_DENIED:
                        
                        switch(this.rreply.rejectStat){
                            case RPC_MISMATCH:
                                break;
                            case AUTH_ERROR:
                                break;
                        }

                        break;
                }


                break;
        }

        return null;
    }


    byte[] encodeBody(CallBody cbody){
        
        int i = 0;
        byte[] temp = new byte[16];

        temp[i++] = (byte)(cbody.rpcVersion >> 24);
        temp[i++] = (byte)(cbody.rpcVersion >> 16);
        temp[i++] = (byte)(cbody.rpcVersion >> 8);
        temp[i++] = (byte)(cbody.rpcVersion);

        temp[i++] = (byte)(cbody.program >> 24);
        temp[i++] = (byte)(cbody.program >> 16);
        temp[i++] = (byte)(cbody.program >> 8);
        temp[i++] = (byte)(cbody.program);

        temp[i++] = (byte)(cbody.pVersion >> 24);
        temp[i++] = (byte)(cbody.pVersion >> 16);
        temp[i++] = (byte)(cbody.pVersion >> 8);
        temp[i++] = (byte)(cbody.pVersion);

        temp[i++] = (byte)(cbody.procedure >> 24);
        temp[i++] = (byte)(cbody.procedure >> 16);
        temp[i++] = (byte)(cbody.procedure >> 8);
        temp[i++] = (byte)(cbody.procedure);
    
        return temp;
    }


    byte[] encodeInt(int num){  // signed
        
        byte[] temp = new byte[4];

        int i = 0;
        temp[i++] = (byte)(num >> 24);
        temp[i++] = (byte)(num >> 16);
        temp[i++] = (byte)(num >> 8);
        temp[i++] = (byte)(num);

        return temp;
    }


    byte[] encodeUnsigedInt(int num){
        
        byte[] temp = new byte[4];

        int i = 0;
        temp[i++] = (byte)(num >>> 24);
        temp[i++] = (byte)(num >>> 16);
        temp[i++] = (byte)(num >>> 8);
        temp[i++] = (byte)(num);

        return temp;
    }


    byte[] encodeEnum (String[] enumerations, String which){

        for(int i = 0 ; i < enumerations.length; i++){
            if (enumerations[i].equals(which)){
                
                byte[] temp = new byte[4];
                int j = i;

                temp[j++] = (byte)(which >> 24);
                temp[j++] = (byte)(which >> 16);
                temp[j++] = (byte)(which >> 8);
                temp[j++] = (byte)(which);

                break;
            } else {
                throw new IllegalArgumentException("Enum ordinal not found");
            }
        }

    }


    byte[] encodeBoolean(String[] enums, String which){
        
        if (enums.length > 2){
            throw new IllegalArgumentException("Booleans can have only length 2");
        } else if(!(enums[0].equals("FALSE"))){
            throw new Exception("First value in enums must be FALSE");
        } else if (!(enums[1].equals("TRUE"))){
            throw new Exception("Second value in enums must be TRUE");
        }

        byte[] temp = new byte[4];
        int i = 0;

        switch(which){
            case "TRUE":
                 int l = 0;
                temp[i++] = (byte)(l >> 24);
                temp[i++] = (byte)(l >> 16);
                temp[i++] = (byte)(l >> 8);
                temp[i++] = (byte)(l);

                break;
            
            case "FALSE":
                int v = 1;
                temp[i++] = (byte)(v >> 24);
                temp[i++] = (byte)(v >> 16);
                temp[i++] = (byte)(v >> 8);
                temp[i++] = (byte)(v);
        }

        return temp;
    }


    byte[] encodeOpaqueVariableLen(byte[] opaqueData){       // simple adds padding
            
            byte[] newOpaque;

            if ((opaqueData.length % 4) == 0){
            
                return opaqueData;
            
            } else {
                
                int pads = opaqueData % 4;
                int i = 0;
                newOpaque = new byte[opaqueData.length + pads];

                for(byte b : opaqueData) newOpaque[i++] = b;

                while(i < newOpaque.length) newOpaque[i++] = 0;    
            }

            return newOpaque;
    }



    public static void main(String[] args){
        System.out.println("xdrencoder live");

        CallBody c = new CallBody(2,1,1,1);
        c.rpcVersion = 2;
        c.program = 1;
        c.pVersion = 1;
        c.procedure = 1;


        XDREncoder e = new XDREncoder(c);
        byte[] bytes = e.encodeRpcMsg(25, MsgType.CALL);


    }
}