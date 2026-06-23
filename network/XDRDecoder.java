import java.util.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


class XDRDecoder { 

    RpcMsg message;
    CallBody cbody;
    ReplyBody rbody;
    AcceptedReply areply;
    RejectedReply rreply;



    void decodeRpcMsg(byte[] wireBytes, MsgType mType) {
        
        switch(mType){
            
            case CALL:

                int xidNum = decodeInt(Arrays.copyOf(wireBytes, 4));
                CallBody body = decodeBody(Arrays.copyOfRange(wireBytes,4, wireBytes.length));


                System.out.println("decoded RpcMessage: xid = "+ xidNum + " body = " + body.program);

                break; 

            case REPLY:


                switch(this.rbody.stat){
                    case MSG_ACCEPTED:

                        switch(this.areply.acceptStat){
                            case SUCCESS:        /* RPC executed successfully       */
                                
                                /*String success = "successs";
                                byte[] s = success.getBytes();
                                byte[] results = new byte[4 + 8];
                                byte[] xi = encodeInt(xid);

                                int k = 0;

                                for(byte b : xi) results[k++] = b;
                                for(byte c : s) s[k++] = c;*/

                                int id = decodeInt(Arrays.copyOf(wireBytes, 4));
                                String suc = new String(wireBytes, 4, wireBytes.length, StandardCharsets.UTF_8);
                                System.out.println("Successful reply: " + id + " str: " + suc);
                                break;
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

        return;
    }



    int decodeInt(byte[] num){
        
        int i = 0;
        int decodedNum = ((num[i++] & 0xFF) << 24) |
                         ((num[i++] & 0xFF) << 16) |
                         ((num[i++] & 0xFF) << 8)  |
                         ((num[i++] & 0xFF));

        return decodedNum;
    }


    CallBody decodeBody(byte[] cbody){
        
        int i = 0;

        int rpcVersion =  ((cbody[i++] & 0xFF) << 24) |
                            ((cbody[i++] & 0xFF) << 16) |
                            ((cbody[i++] & 0xFF) << 8)  |
                            ((cbody[i++] & 0xFF));

        int program =  ((cbody[i++] & 0xFF) << 24) |
                        ((cbody[i++] & 0xFF) << 16) |
                        ((cbody[i++] & 0xFF) << 8)  |
                        ((cbody[i++] & 0xFF));

        int pVersion = ((cbody[i++] & 0xFF) << 24) |
                        ((cbody[i++] & 0xFF) << 16) |
                        ((cbody[i++] & 0xFF) << 8)  |
                        ((cbody[i++] & 0xFF));

        int procedure = ((cbody[i++] & 0xFF) >> 24) |
                        ((cbody[i++] & 0xFF) >> 16) |
                        ((cbody[i++] & 0xFF) >> 8)  |
                        ((cbody[i++] & 0xFF));

        CallBody c = new CallBody(rpcVersion, program, pVersion, procedure);
        
        return c;
    }




    public static void main(String[] args){
        System.out.println("decoder");

        byte[] wireBytes = {0, 0, 0, 25, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1};
        
        XDRDecoder d = new XDRDecoder();
        d.decodeRpcMsg(wireBytes, MsgType.CALL);

    }
}
