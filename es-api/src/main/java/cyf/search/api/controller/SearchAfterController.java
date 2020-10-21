package cyf.search.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cyf.search.api.service.SearchAfterAndScrollService;
import cyf.search.api.service.SuggestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Cheng Yufei
 * @create 2020-10-12 10:30
 **/
@RestController
@RequestMapping("/suggest")
public class SearchAfterController {

	@Autowired
	private SearchAfterAndScrollService service;

	@GetMapping("/searchAfter")
	public List<ObjectNode> searchAfter(@RequestParam String[] param) {
		return service.searchAfter(param);
	}

}
