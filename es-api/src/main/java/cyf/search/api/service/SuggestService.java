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
				//短语suggester使用候选生成器生成给定文本中每个术语的可能术语列表
				//单个候选生成器类似于为文本中的每个单独术语调用的term suggest
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

	/**
	 *上下文提示：
	 * 	 两种类型：category 或 geo
	 *
	 *mapping：
	 *POST context_suggest/A/_mapping
	 * {
	 *   "properties": {
	 *     "id": {
	 *       "type": "integer"
	 *     },
	 *     "contents": {
	 *       "type": "completion",
	 *       "contexts": [
	 *         {
	 *           "name": "con_type",
	 *           "type": "category"
	 *         }
	 *       ]
	 *     }
	 *   }
	 * }
	 *
	 *插入数据：
	 * PUT context_suggest/A/1
	 * {
	 *   "id":1,
	 *   "contents":{
	 *     "input":["长安一片月，万户捣衣声。秋风吹不尽，总是玉关情。何日平胡虏，良人罢远征？","镜湖三百里，菡萏发荷花。五月西施采，人看隘若耶。回舟不待月，归去越王家。"],
	 *     "contexts":{
	 *       "con_type":["poetry"]
	 *     }
	 *   }
	 * }
	 *
	 * PUT context_suggest/A/2
	 * {
	 *   "id":1,
	 *   "contents":{
	 *     "input":["长安街"],
	 *     "contexts":{
	 *       "con_type":["location"]
	 *     }
	 *   }
	 * }
	 *
	 * 搜索建议：
	 * GET context_suggest/A/_search
	 * {
	 *   "suggest":{
	 *     "my_context_sugg":{
	 *       "prefix":"长安",
	 *       "completion":{
	 *         "field":"contents",
	 *         "contexts":{
	 *           "con_type":["poetry"]
	 *         }
	 *       }
	 *     }
	 *   }
	 * }
	 *
	 *
	 * @param text
	 * @param category
	 * @return
	 */
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
