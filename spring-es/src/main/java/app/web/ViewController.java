package app.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author faith.huan 2019-10-22 21:25
 */
@Controller
public class ViewController {

    @RequestMapping("search/index")
    public String searchIndex(){
        return "search/index";
    }

}
