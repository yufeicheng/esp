package cyf.search.api.controller;

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
public class SuggestController {

	@Autowired
	private SuggestService suggestService;

	@GetMapping("/term")
	public List<String> termSuggest(@RequestParam String text) {
		List<String> strings = suggestService.termSuggest(text);
		return strings;
	}

	@GetMapping("/phrase")
	public List<String> phraseSuggest(@RequestParam String text) {
		List<String> strings = suggestService.phraseSuggest(text);
		return strings;
	}

	@GetMapping("/context")
	public List<String> contextSuggest(@RequestParam String text, @RequestParam String category) {
		List<String> strings = suggestService.contextsSuggest(text, category);
		return strings;
	}
}
