/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.baseio.common;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangkai
 *
 */
public class EmojiUtil {

	public static String EMOJI_ALL = "🏠🏡🏫🏢🏣🏥🏦🏪🏩🏨💒⛪️🏬🏤🌇🌆🌄🗻🗾🗼🏭⛺️🏰🏯😊😍😘😳😡😓😭😲😁😱😖😉😏😜😰😢😚😄😪😣😔😠😌😝😂😥😃😨😒😷😞👿👽😁😄😇😯😕😂😅😈"
			+ "😐😠😀😃😆😉😑😬😮😥😨😟😢😣😦😩😱😵😴😤😧😰😶😷😝😙😎😖😞😛😋😭😔😒😜😗😚😌😪😏🙋🙅🙎😼😻🙌🙆🙏😸😽😫🙍🙇☺😁😄😇😯😕😂😅😈😐😠😀😃😆😉😑"
			+ "😬😮😥😨😟😢😣😦😩😱😵😴😤😧😰😶😷😝😙😎😖😞😛😋😭😔😒😜😗😚😌😪😏🙋🙅🙎😼😻🙌🙆🙏😸😽😫🙍🙇😺😹😿😾🙉👶👨👵🙀🙊👦👩😄😃😀☺️😉😚😗😙😜😝😛😁"
			+ "😔😌😒😞😣😢😂😭😪😥😰😅😩😫😨😱😠😤😖😆😋😷😎😴😵😟😦😧😈👿😮😬😐😕😯😶😇😏😑👲👳👮👷💂👶👦👧👨👩👴👵👱👼👸😺😸😻😽😼🙀😿😹😾👹👺🙈🙉🙊💀👽👀👃"
			+ "👄👂❤💔💘💝💜💛💚💙💩👍👎👊✌👌💪👆👇👈👉✊👐🙏🙌👏👧👦👩👨👶👵👴👳👳👳👲👸👸👷💂👮🙆🙅💇🙅💇💆💁💁👯👫👫🎎🚶🏃💃💑💏👼💀🐱🐶🐭🐹🐰🐺🐸🐯🐨🐻"
			+ "🐷🐮🐗🐵🐙🐛🐔🐧🐦🐍🐴🐠🐳🐬☀☔🌙✨⭐⚡☁⛄🌊❗❓🌻🌺🌹🔥🎵💦💤🌷🌸💐🍀🌾🍃🍂🎃👻🎅🌵🌴🎍🍁🎄🔔🎉🎈💿📷🎥📬💡🔑🔒🔓📺💻🛀💰🔫💊💣⚽🏈🏀🎾🎿🏄🏊🏆"
			+ "👾🎤🎸👙👑🌂👜💄💅💍🎁💎☕🎂🍰🍺🍻🍸🍵🍶🍔🍟🍝🍜🍧🍦🍡🍙🍘🍞🍛🍚🍲🍱🍣🍎🍓🍉🍆🍅🍊🚀🚄🚉🚃🚗🚕🚓🚒🚑🚙🚲🏁🚹🚺⭕❌😺😹😿😾🙉👶👨👵🙀🙊👦👩💏🙈"
			+ "💩👧👴💑👪👫👬👭👮💂👸👱💃👤👷👯🎅👲💆👥💁👰👼👳💇💅👺👿👀👣💋👻👽💀👂👄❤👹👾💪👃👅💙💚💓💖💝👍✊💛💔💗💞👎✌💜💕💘💟👌✋👊👇👋☝👈👏👆👉👐🔰👟"
			+ "🎩⌚👖👙💄👑🎓👔👗👠👞👒👓👕👘👡👢💼👛💲💶💱👚🎒💰💵💷💹👜👝💳💴💸🔫🔪💊🔕🔭🔋📗💣🚬🚪🔮🔌📘💉🔔🔬🔦📜📙📚📑📖🎃🎁🎆📔📓📰🎄🎂🎇📒📕📛🎀🎈🎉🎊🎌"
			+ "🎎📟📠📨🎍🎐📱☎📦📩🎏🎋📲📞✉📪📫📮📯📡✒📏📭📤📢💬✏📐📬📥📣💭📝📍📌💺💾📅📁📄📎💻💿📇📂📊✂💽📆📋📃📈📉🎢🎨📷🎭🎲⛺🎠🎬📹🎫🎰🎡🎪🎥🎦🎮🃏🎴📺"
			+ "📼🎵🎻🎺🀄📻🎧🎶🎹🎸🎯📀🎤🎼🎷〽🐕🐈🐁🐢🐓🐤🐶🐱🐭🐇🐔🐥🐩🐀🐹🐰🐣🐦🐏🐺🐄🐗🐽🐼🐑🐃🐮🐖🐸🐧🐐🐂🐴🐷🐍🐘🐨🐆🐫🐳🐠🐚🐒🐯🐪🐋🐡🐬🐵🐻🐊🐟🐙🐌🐛🐞"
			+ "🐾🍻🍶🍼🐜🐲🍸🍷☕🍴🐝🐉🍺🍹🍵🍨🍧🍰🍬🍯🍟🍖🍦🍪🍭🍳🍝🍗🍩🍫🍮🍔🍕🍤🍣🍜🍛🍢🍠🍏🍱🍙🍲🍡🍌🍊🍞🍚🍥🍘🍎🍋🍄🍇🍐🍓🌴🌴🍅🍈🍑🍍🌲🌵🍆🍉🍒🌰🌳🌷🌸"
			+ "🍁🌺🌽☀☁🌹🍂🌻🌾🌈🌁🍀🍃🌼🌿⛅🌂☔🌀🌙🌚🌑🌔💧❄🌞🌛🌒🌕⚡⛄🌝🌜🌓🌖🌗🌄🌆🌉🌎🌐🌘🌅🌃🌊🌏🌟🎑🌇🌌🌋🌍🌠🏠🏣🏦🏩⛪🏯🏡🏤🏧🏪⛲🏰🏢🏥🏨🏫🏬🏭🗻"
			+ "🗾🏮🔨🛁🚾🗼🗿💈🔩🛀🎽🗽⚓🔧🚿🚽🎣🎱🎿🏂🏂🏆🏈🎳🎾🏀🏃🏇🏉⚾⚽🏁🏄🐎🏊🚂🚅Ⓜ🚋🚎🚑🚃🚆🚈🚌🚏🚒🚄🚇🚊🚍🚐🚓🚔🚗🚚🚝🚠🚣🚕🚘🚛🚞🚡🚁🚖🚙🚜🚢🚢✈🛂"
			+ "🛅🚳🚷🚀⛽🛃⛵🚴🚸🚤🅿🛄🚲🚵🚉🚶🚥🚦♨💎🚧💌💐🚨💍💒💏🙈💩👧👴💑👪👫👬👭👮💂👸👱💃👤👷👯🎅👲💆👥💁👰👼👳💇💅👺👿👀👣💋👻👽💀👂👄❤👹👾💪👃👅💙💚💓"
			+ "💖💝👍✊💛💔💗💞👎✌💜💕💘💟👌✋👊👇👋☝👈👏👆👉👐🔰👟🎩⌚👖👙💄👑🎓👔👗👠👞👒👓👕👘👡👢💼👛💲💶💱👚🎒💰💵💷💹👜👝💳💴💸🔫🔪💊🔕🔭🔋📗💣🚬🚪🔮🔌📘💉"
			+ "🔔🔬🔦📜📙📚📑📖🎃🎁🎆📔📓📰🎄🎂🎇📒📕📛🎀🎈🎉🎊🎌🎎📟📠📨🎍🎐📱☎📦📩🎏🎋📲📞✉📪📫📮📯📡✒📏📭📤📢💬✏📐📬📥📣💭📝📍📌💺💾📅📁📄📎💻💿📇📂📊✂"
			+ "💽📆📋📃📈📉🎢🎨📷🎭🎲⛺🎠🎬📹🎫🎰🎡🎪🎥🎦🎮🃏🎴📺📼🎵🎻🎺🀄📻🎧🎶🎹🎸🎯📀🎤🎼🎷〽🐕🐈🐁🐢🐓🐤🐶🐱🐭🐇🐔🐥🐩🐀🐹🐰🐣🐦🐏🐺🐄🐗🐽🐼🐑🐃🐮🐖🐸🐧🐐🐂"
			+ "🐴🐷🐍🐘🐨🐆🐫🐳🐠🐚🐒🐯🐪🐋🐡🐬🐵🐻🐊🐟🐙🐌🐛🐞🐾🍻🍶🍼🐜🐲🍸🍷☕🍴🐝🐉🍺🍹🍵🍨🍧🍰🍬🍯🍟🍖🍦🍪🍭🍳🍝🍗🍩🍫💩🔥✨🌟💫💥💢💦💧💤💨👂👀👃👅👄👍👎👌"
			+ "👊✊✌️👋✋👐👆👇👉👈🙌🙏☝️👏💪🚶🏃💃👫👪👬👭💏💑👯🙆🙅💁🙋💆💇💅👰🙎🙍🙇🎩👑👒👟👞👡👠👢👕👔👚👗🎽👖👘👙💼👜👝👛👓🎀🌂💄💛💙💜💚❤️💔💗💓💕💖💞💘💌💋💍"
			+ "💎👤👥💬👣💭🐶🐺🐱🐭🐹🐰🐸🐯🐨🐻🐷🐽🐮🐗🐵🐒🐴🐑🐘🐼🐧🐦🐤🐥🐣🐔🐍🐢🐛🐝🐜🐞🐌🐙🐚🐠🐟🐬🐳🐋🐄🐏🐀🐃🐅🐇🐉🐎🐐🐓🐕🐖🐁🐂🐲🐡🐊🐫🐪🐆🐈🐩🐾💐🌸🌷🍀🌹"
			+ "🌻🌺🍁🍃🍂🌿🌾🍄🌵🌴🌲🌳🌰🌱🌼🌐🌞🌝🌚🌑🌒🌓🌔🌕🌖🌗🌘🌜🌛🌙🌍🌎🌏🌋🌌🌠⭐️☀️⛅️☁️⚡️☔️❄️⛄️🌀🌁🌈🌊🎍💝🎎🎒🎓🎏🎆🎇🎐🎑🎃👻🎅🎄🎁🎋🎉🎊🎈🎌🔮🎥📷📹📼💿📀"
			+ "💽💾💻📱☎️📞📟📠📡📺📻🔊🔉🔈🔇🔔🔕📢📣⏳⌛️⏰⌚️🔓🔒🔏🔐🔑🔎💡🔦🔆🔅🔌🔋🔍🛁🛀🚿🚽🔧🔩🔨🚪🚬💣🔫🔪💊💉💰💴💵💷💶💳💸📲📧📥📤✉️📩📨📯📫📪📬📭📮📦📝📄📃📑"
			+ "📊📈📉📜📋📅📆📇📁📂✂️📌📎✒️✏️📏📐📕📗📘📙📓📔📒📚📖🔖📛🔬🔭📰🎨🎬🎤🎧🎼🎵🎶🎹🎻🎺🎷🎸👾🎮🃏🎴🀄️🎲🎯🏈🏀⚽️⚾️🎾🎱🏉🎳⛳️🚵🚴🏁🏇🏆🎿🏂🏊🏄🎣☕️🍵🍶🍼🍺🍻"
			+ "🍸🍹🍷🍴🍕🍔🍟🍗🍖🍝🍛🍤🍱🍣🍥🍙🍘🍚🍜🍲🍢🍡🍳🍞🍩🍮🍦🍨🍧🎂🍰🍪🍫🍬🍭🍯🍎🍏🍊🍋🍒🍇🍉🍓🍑🍈🍌🍐🍍🍠🍆🍅🌽🏠🏡🏫🏢🏣🏥🏦🏪🏩🏨💒⛪️🏬🏤🌇🌆🏯🏰⛺️🏭"
			+ "🗼🗾🗻🌄🌅🌃🗽🌉🎠🎡⛲️🎢🚢⛵️🚤🚣⚓️🚀✈️💺🚁🚂🚊🚉🚞🚆🚄🚅🚈🚇🚝🚋🚃🚎🚌🚍🚙🚘🚗🚕🚖🚛🚚🚨🚓🚔🚒🚑🚐🚲🚡🚟🚠🚜💈🚏🎫🚦🚥⚠️🚧🔰⛽️🏮🎰♨️🗿🎪🎭📍🚩";

	public static void main(String[] args) {

		test();

	}

	static void test() {

		//		System.out.println(emoji);
		byte[] bb = "✨".getBytes();
		byte[] array = EMOJI_ALL.getBytes();
		List<String> list = bytes2Emojis(array);
		System.out.println(new String(array));
		System.out.println(new String(array, 0, 2, Encoding.UTF8));
		System.out.println(EMOJI_ALL);
		System.out.println(EMOJI_ALL.length());
		System.out.println(EMOJI_ALL.getBytes().length);

	}

	public static List<String> bytes2Emojis(byte[] array) {
		List<String> list = new ArrayList<>(array.length * 2 / 7);
		for (int i = 0; i < array.length;) {
			if (array[i] == -16) {
				list.add(new String(array, i, 4,Encoding.UTF8));
				i += 4;
			} else {
				//array[i] == -30 || array[i] == -17 || array[i] == -29
				list.add(new String(array, i, 3,Encoding.UTF8));
				i += 3;
			}
		}
		return list;
	}

}
