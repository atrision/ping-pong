package com.misuzu.back;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;
import com.openai.models.ChatCompletionUserMessageParam;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackApplicationTests {

	@Test
	void contextLoads() {
		OpenAIClient client = OpenAIOkHttpClient.builder()
				.apiKey("bce-v3/ALTAK-Gt7uW3i1DPt574mxdNJ4h/28abd2d42cbeb5fd624db7ac634fa73a09a40c2e") //将your_APIKey替换为真实值，如何获取API Key请查看https://cloud.baidu.com/doc/WENXINWORKSHOP/s/Um2wxbaps#步骤二-获取api-key
				.baseUrl("https://qianfan.baidubce.com/v2/chat/completions") //千帆ModelBuilder平台地址
				.build();

		ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
				.addUserMessage("你好") // 对话messages信息
				.model("ernie-4.0-8k-latest") // 模型对应的model值，请查看支持的模型列表：https://cloud.baidu.com/doc/WENXINWORKSHOP/s/wm7ltcvgc
				.build();

		ChatCompletion chatCompletion = client.chat().completions().create(params);
		System.out.println(chatCompletion.choices().get(0).message().content());

	}

}
