package eu.cadmus.vulnshop.mvc;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * ============================================================================
 *  MVC error handling
 * ============================================================================
 *
 * A custom error page, added during an incident so that on-call engineers
 * could see what had gone wrong without opening the server logs. It was never
 * reverted, and the error/ template it renders is served to every visitor.
 *
 * Ask question four from N1: what happens when it fails, and who is told?
 */
@Controller
public class GlobalErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Throwable ex = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        model.addAttribute("status", status);
        model.addAttribute("message", message);
        model.addAttribute("path", path);

        if (ex != null) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            model.addAttribute("trace", sw.toString());
            model.addAttribute("exceptionClass", ex.getClass().getName());
        }

        return "error";
    }
}
