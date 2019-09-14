package cyf.search.base.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.htmlparser.Parser;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.TextExtractingVisitor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HtmlUtils {
	

	public static void main(String[] args) {
//		System.out.println(extractText("<font color='#009900'><b></b>20020<span>我们大家</span></font>"));
//		
//		System.out.println(".class".replaceAll("//.", "//////$"));  
//		System.out.println(".class".replaceAll("//Q.//E", "//////$"));  
//		System.out.println(".class".replaceAll(Pattern.quote("."), Matcher.quoteReplacement("//$"))); 
		
		String s = "<span class=\"editor-insert-stock\" data-code=\"600002\">$<a href=\"http://stock.jrj.com.cn/share,600002.shtml\" class=\"link\" target=\"_blank\">齐鲁石化(600002)</a>$<span></span></span>我嗡嗡嗡嗡嗡嗡嗡嗡嗡";
		//<p>请问<span class="editor-insert-stock" data-code="600030">$<a href="http://stock.jrj.com.cn/share,600030.shtml" class="link" target="_blank">中信证券(600030)</a>$或者其他券商股，还能追吗？</span></p>
		String ss = extractText(s);
		System.out.println(ss);
		System.out.println(s.replaceAll("\\$", "")); 
		String sss = getPlainText(s);
		System.out.println(sss);
	}

	/**
	 * html 内容抽取，包括去除无意义字符
	 * 
	 * @param inputHtml
	 * @return
	 */
	public static String extractText(String inputHtml) {
		if (StringUtils.isEmpty(inputHtml))
			return "";
//		inputHtml = inputHtml.replaceAll("\\$", "");
//		inputHtml = inputHtml.replaceAll("\\(", "");
//		inputHtml = inputHtml.replaceAll("\\)", "");
		//inputHtml = replaceXmlEntity(inputHtml);
		try {
			Parser parser = Parser.createParser(inputHtml, "GBK");
			//StringBean sb = new StringBean();
			
			TextExtractingVisitor visitor = new TextExtractingVisitor();
//			// 设置不需要得到页面所包含的链接信息
//			sb.setLinks(false);
//			// 设置将不间断空格由正规空格所替代
//			sb.setReplaceNonBreakingSpaces(true);
//			// 设置将一序列空格由一个单一空格所代替
//			sb.setCollapse(false);
			parser.visitAllNodesWith(visitor);
			//visitor.getExtractedText();
			String content = visitor.getExtractedText();
			if (!StringUtils.isEmpty(content)) {
				content = content.replaceAll("“", "\"");
				content = content.replaceAll("\\?", "?");
				content = content.replaceAll("&#8205", "");
				content = content.replaceAll("&nbsp;", " ");
				content = content.replaceAll("”", "\"");
				content = content.replaceAll("<!\\[CDATA\\[", "");
				content = content.replaceAll("]]>", "");
				content = content.replaceAll("\n", "");
				content = content.replaceAll("\r\n", "");
				return content;
			}
			return "";
		} catch (Exception e) {
			return "";
		}
	}

	public static String replaceXmlEntity(String content) {
		if (StringUtils.isEmpty(content) == false) {
			content = content.replaceFirst("\\?", "");
			Pattern pattern = Pattern.compile("(&#\\d*;)");
			Matcher matcher = pattern.matcher(content);
			if (matcher.find()) {
				return matcher.replaceAll("");
			}
			return content;
		}
		return "";
	}

	/**
	 * 抽取纯文本
	 * @param content
	 * @return
	 */
	public static String getPlainText(String content) {
		if(StringUtils.isEmpty(content)){
			return content;
		}
		Parser parser = null;
		String s = null;
		try {
			parser = new Parser(content);
			TextExtractingVisitor visitor = new TextExtractingVisitor();
			parser.visitAllNodesWith(visitor);
			s = visitor.getExtractedText();
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	/**
	 * 过滤UnicodeString
	 * @author biaoqi.du
	 * @param value
	 * @return
	 */
	public static String filterUnicodeString(String value) {
		if (value == null) {
			return null;
		}
		char[] xmlChar = value.toCharArray();
		for (int i = 0; i < xmlChar.length; i++) {
			if (xmlChar[i] > 0xFFFD) {
				xmlChar[i] = ' ';// 用空格替换
			} else if (xmlChar[i] < 0x20 && xmlChar[i] != 't'
					& xmlChar[i] != 'n' & xmlChar[i] != 'r') {
				xmlChar[i] = ' ';// 用空格替换
			}
		}
		return new String(xmlChar);
	}

}
