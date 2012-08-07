package org.weeels.dispatcher.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.weeels.dispatcher.domain.RideProposal;
import org.weeels.dispatcher.domain.RideRequest;
import org.weeels.dispatcher.domain.Rider;
import org.weeels.dispatcher.repository.RideBookingRepository;
import org.weeels.dispatcher.repository.RideProposalRepository;
import org.weeels.dispatcher.repository.RideRequestRepository;

@RequestMapping("/rideproposals")
@Controller
public class RideProposalController {
	
    @Autowired
    RideBookingRepository rideBookingRepository;
    
    @Autowired
    RideRequestRepository rideRequestRepository;
    
    @Autowired
    RideProposalRepository rideProposalRepository;
	
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid RideProposal rideProposal, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, rideProposal);
            return "rideproposals/create";
        }
        uiModel.asMap().clear();
        rideProposalRepository.save(rideProposal);
        return "redirect:/rideproposals/" + encodeUrlPathSegment(rideProposal.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new RideProposal());
        return "rideproposals/create";
    }
    
    @RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") String id, Model uiModel) {
    	uiModel.addAttribute("rideproposal", rideProposalRepository.findOne(id));
    	uiModel.addAttribute("itemId",id);
        return "rideproposals/show";
    }
    
	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("rideproposals", rideProposalRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / sizeNo, sizeNo)).getContent());
            float nrOfPages = (float) rideProposalRepository.count() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("rideproposals", rideProposalRepository.findAll());
        }
        return "rideproposals/list";
    }
    
    
    @RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid RideProposal rideProposal, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, rideProposal);
            return "rideproposals/update";
        }
        uiModel.asMap().clear();
        rideProposalRepository.save(rideProposal);
        return "redirect:/rideproposals/" + encodeUrlPathSegment(rideProposal.getId().toString(), httpServletRequest);
    }
    
    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") String id, Model uiModel) {
        populateEditForm(uiModel, rideProposalRepository.findOne(id));
        return "rideproposals/update";
    }
    
    
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") String id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        RideProposal rideProposal = rideProposalRepository.findOne(id);
        rideProposalRepository.delete(rideProposal);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/rideproposals";
    }
    
    void populateEditForm(Model uiModel, RideProposal rideProposal) {
        uiModel.addAttribute("rideproposal", rideProposal);
        uiModel.addAttribute("ridebookings", rideBookingRepository.findAll());
        uiModel.addAttribute("riderequests", rideRequestRepository.findAll());
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
        RideProposal rideProposal = rideProposalRepository.findOne(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        if (rideProposal == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(rideProposal.toJson(), headers, HttpStatus.OK);
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        List<RideProposal> result = rideProposalRepository.findAll();
        return new ResponseEntity<String>(RideProposal.toJsonArray(result), headers, HttpStatus.OK);
    }

	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJson(@RequestBody String json) {
        RideProposal rideProposal = RideProposal.fromJsonToRideProposal(json);
        rideProposalRepository.save(rideProposal);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
        for (RideProposal rideProposal: RideProposal.fromJsonArrayToRideProposals(json)) {
            rideProposalRepository.save(rideProposal);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

	@RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJson(@RequestBody String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        RideProposal rideProposal = RideProposal.fromJsonToRideProposal(json);
        if (rideProposalRepository.save(rideProposal) == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        for (RideProposal rideProposal: RideProposal.fromJsonArrayToRideProposals(json)) {
            if (rideProposalRepository.save(rideProposal) == null) {
                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<String> deleteFromJson(@PathVariable("id") String id) {
        RideProposal rideProposal = rideProposalRepository.findOne(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        if (rideProposal == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        rideProposalRepository.delete(rideProposal);
        return new ResponseEntity<String>(headers, HttpStatus.OK);
    }
}
