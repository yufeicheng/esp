package cyf.search.api.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import cyf.search.base.enums.IndexType;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.completion.context.CategoryQueryContext;
import org.elasticsearch.search.suggest.phrase.DirectCandidateGenerator;
import org.elasticsearch.search.suggest.phrase.DirectCandidateGeneratorBuilder;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestionBuilder;
import org.elasticsearch.search.suggest.term.TermSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cheng Yufei
 * @create 2020-10-12 10:12
 **/
@Service
@Slf4j
public class SuggestService {

	@Autowired
	private TransportClient client;

	/**
	 * 词项建议：
	 * 		1.对输入的text分词后，对每个词项建议
	 * 		2.默认suggest_mode：missing，索引中不存在时才会提示，eg：输入李四，建议为李白,也可理解为是对单词的纠错
	 * 		3.默认min_word_length：4，建议项长度达到4时，才会出现
	 *
	 * 	GET poetry/A/_search
	 * {
	 *   "suggest":{
	 *     "my_term_suggest":{
	 *       "text":"清溪深不测",
	 *       "term":{
	 *       	"size":10,
	 *         "suggest_mode":"missing",
	 *         "field":"contents",
	 *         "min_word_length":1
	 *       }
	 *     }
	 *   }
	 * }
	 *
	 * @param text
	 * @return
	 */
	public List<String> termSuggest(String text) {
		TermSuggestionBuilder termSuggestionBuilder = SuggestBuilders.termSuggestion("contents").text(text).suggestMode(TermSuggestionBuilder.SuggestMode.ALWAYS)
				.minWordLength(2);

		SuggestBuilder suggestBuilder = new SuggestBuilder();
		suggestBuilder.addSuggestion("my_term_suggest", termSuggestionBuilder);
		SearchRequestBuilder suggest = client.prepareSearch(IndexType.POETRY.getIndex()).setTypes(IndexType.POETRY.getType()).suggest(suggestBuilder);
		log.info("termSuggest,dsl:{}", suggest.toString());
		SearchResponse searchResponse = suggest.get();
		ArrayList<String> result = new ArrayList<>();
		List<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> entries = searchResponse.getSuggest().getSuggestion("my_term_suggest").getEntries();
		entries.stream().forEach(e -> {
			List<? extends Suggest.Suggestion.Entry.Option> options = e.getOptions();
			options.stream().forEach(o -> {
				String string = o.getText().string();
				result.add(string);
			});
		});
		return result;
	}

	/**
	 * 短语建议：会考虑多个词项的关系，
	 *
	 * GET poetry/A/_search
	 * {
	 *
	 *   "suggest":{
	 *     "text":"清溪深不",
	 *     "my_s":{
	 *       "phrase":{
	 *         "max_errors": 7,
	 *         "confidence": 0,
	 *         "field":"contents",
	 *           "highlight": {
	 *           "pre_tag": "<em>",
	 *           "post_tag": "</em>"
	 *         }
	 *         ,
	 *          "direct_generator": [ {
	 *           "field": "contents",
	 *           "suggest_mode": "missing",
	 *           "min_word_length":1
	 *         } ]
	 *       }
	 *     }
	 *   }
	 * }
	 * @param text
	 * @return
	 */
	public List<String> phraseSuggest(String text) {

		DirectCandidateGeneratorBuilder generatorBuilder = new DirectCandidateGeneratorBuilder("contents");
		generatorBuilder.suggestMode("popular");
		generatorBuilder.minWordLength(1);

		PhraseSuggestionBuilder phraseBuilder = SuggestBuilders.phraseSuggestion("contents").text(text).confidence(0f).highlight("<em>", "</em>")
				//
				.addCandidateGenerator(generatorBuilder);

		SuggestBuilder suggestBuilder = new SuggestBuilder();
		suggestBuilder.addSuggestion("my_phrase_suggest", phraseBuilder);

		SearchRequestBuilder requestBuilder = client.prepareSearch(IndexType.POETRY.getIndex()).setTypes(IndexType.POETRY.getType()).suggest(suggestBuilder);
		log.info("phraseSuggest,dsl:{}", requestBuilder.toString());

		SearchResponse searchResponse = requestBuilder.get();

		ArrayList<String> result = new ArrayList<>();
		return result;
	}

	public List<String> completionSuggest(String text) {

		ArrayList<String> result = new ArrayList<>();
		return result;
	}

	public List<String> contextsSuggest(String text, String category) {

		ArrayList<String> result = new ArrayList<>();

		CategoryQueryContext categoryQueryContext = CategoryQueryContext.builder().setCategory(category).build();

		CompletionSuggestionBuilder suggestionBuilder = SuggestBuilders.completionSuggestion("contents").prefix(text)
				.contexts(ImmutableMap.of("con_type", Lists.newArrayList(categoryQueryContext)));

		SuggestBuilder suggestBuilder = new SuggestBuilder();
		suggestBuilder.addSuggestion("my_context_suggest", suggestionBuilder);
		SearchRequestBuilder suggest = client.prepareSearch(IndexType.CONTEXT_SUGGEST.getIndex()).setTypes(IndexType.CONTEXT_SUGGEST.getType()).suggest(suggestBuilder);
		log.info("contextSuggest,dsl:{}", suggest.toString());
		SearchResponse searchResponse = suggest.get();
		List<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> context_suggest = searchResponse.getSuggest().getSuggestion("my_context_suggest").getEntries();
		context_suggest.stream().forEach(c -> {
			List<? extends Suggest.Suggestion.Entry.Option> options = c.getOptions();
			options.stream().forEach(o -> {
				result.add(o.getText().string());
			});
		});

		return result;
	}
}
