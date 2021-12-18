package com.ricardo.demo.service.idempotency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class TokenService {

    @Autowired RedisService redisService;

    //Create token
    public String createToken() {
        //Use UUID for token
        UUID uuid = UUID.randomUUID();
        String token = uuid.toString();
        //Deposit in redis
        boolean b = redisService.setEx(token, token, 10000L);
        return b?token:null;
    }

    //Verify whether there is a token in the request header or request parameter
    public String checkToken(String token ) {
        if(token==null || token==""){
            return ("Missing parameter token");
        }
        if(!redisService.exists(token)){
            return ("Cannot submit repeatedly-------token Incorrect");
        }
        //Token remove token correctly
        if(!redisService.remove(token)){
            return ("Token Remove failed");
        }
        return ("OK");
    }
}
