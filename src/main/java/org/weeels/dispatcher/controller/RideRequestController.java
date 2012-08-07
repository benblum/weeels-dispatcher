package org.weeels.dispatcher.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.joda.time.format.DateTimeFormat;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.lms.message.RideRequestMessage;
import org.weeels.dispatcher.repository.RideBookingRepository;
import org.weeels.dispatcher.repository.RideRequestRepository;
import org.weeels.dispatcher.repository.RiderRepository;

@RequestMapping("/riderequests")
@Controller
public class RideRequestController {
	
	@Autowired
    RideRequestRepository rideRequestRepository;

	@Autowired
    RideBookingRepository rideBookingRepository;

	@Autowired
    RiderRepository riderRepository;
	
	@Autowired
	RabbitTemplate requestTemplate;
	
	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid RideRequest rideRequest, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, rideRequest);
            return "riderequests/create";
        }
        uiModel.asMap().clear();
        rideRequest.setStatus(RideRequest.RequestStatus.OPEN);
        rideRequest.setRequestTime(System.currentTimeMillis());
        rideRequestRepository.save(rideRequest);
        return "redirect:/riderequests/" + encodeUrlPathSegment(rideRequest.getId().toString(), httpServletRequest);
    }
	
	@RequestMapping(value="/lms", method = RequestMethod.POST, produces = "text/html")
    public String createLMS(@Valid RideRequestMessage rideRequestMessage, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateLMSEditForm(uiModel, rideRequestMessage);
            return "riderequests/lms";
        }
        uiModel.asMap().clear();
        requestTemplate.convertAndSend(rideRequestMessage);
        return "redirect:/riderequests/" + encodeUrlPathSegment(rideRequestMessage.requestId, httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new RideRequest());
        return "riderequests/create";
    }
	
	@RequestMapping(params = "lms", produces = "text/html")
	public String createLMSForm(Model uiModel) {
		populateLMSEditForm(uiModel, new RideRequestMessage());
		return "riderequests/lms";
	}

	@RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") String id, Model uiModel) {
        //addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("riderequest", rideRequestRepository.findOne(id));
        uiModel.addAttribute("itemId", id);
        return "riderequests/show";
    }

	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("riderequests", rideRequestRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / sizeNo, sizeNo)).getContent());
            float nrOfPages = (float) rideRequestRepository.count() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("riderequests", rideRequestRepository.findAll());
        }
        //addDateTimeFormatPatterns(uiModel);
        return "riderequests/list";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid RideRequest rideRequest, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, rideRequest);
            return "riderequests/update";
        }
        uiModel.asMap().clear();
        rideRequestRepository.save(rideRequest);
        return "redirect:/riderequests/" + encodeUrlPathSegment(rideRequest.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") String id, Model uiModel) {
        populateEditForm(uiModel, rideRequestRepository.findOne(id));
        return "riderequests/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") String id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        RideRequest rideRequest = rideRequestRepository.findOne(id);
        rideRequestRepository.delete(rideRequest);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/riderequests";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("rideRequest_requesttime_date_format", DateTimeFormat.patternForStyle("M-", LocaleContextHolder.getLocale()));
    }

	void populateEditForm(Model uiModel, RideRequest rideRequest) {
        uiModel.addAttribute("rideRequest", rideRequest);
        //addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("riders", riderRepository.findAll());
    }
	
	void populateLMSEditForm(Model uiModel, RideRequestMessage msg) {
        uiModel.addAttribute("rideRequestMessage", msg);
    }

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        } catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }
	
	@RequestMapping(value = "/{id}", headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> showJson(@PathVariable("id") String id) {
        RideRequest rideRequest = rideRequestRepository.findOne(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        if (rideRequest == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(rideRequest.toJson(), headers, HttpStatus.OK);
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        List<RideRequest> result = rideRequestRepository.findAll();
        return new ResponseEntity<String>(RideRequest.toJsonArray(result), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) {
        RideRequest rideRequest = RideRequest.fromJsonToRideRequest(json);
        rideRequestRepository.save(rideRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
        for (RideRequest rideRequest: RideRequest.fromJsonArrayToRideRequests(json)) {
            rideRequestRepository.save(rideRequest);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJson(@RequestBody String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        RideRequest rideRequest = RideRequest.fromJsonToRideRequest(json);
        if (rideRequestRepository.save(rideRequest) == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        for (RideRequest rideRequest: RideRequest.fromJsonArrayToRideRequests(json)) {
            if (rideRequestRepository.save(rideRequest) == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") String id) {
        RideRequest rideRequest = rideRequestRepository.findOne(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        if (rideRequest == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        rideRequestRepository.delete(rideRequest);
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }
}
