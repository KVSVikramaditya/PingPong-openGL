**Vinay:** This is the one.


**Vikram:** Okay. And so, eVestment Market Equity Ranking... Okay.

**Vinay:** Alright, so... this has the epic. Sorry. We don't need to do anything with the epic... within this epic... Has a Jira ticket not been created for this yet?

**Vikram:** So, Market Equity Ranking with Perp link, I think we have created a Jira with all the details, but...

**Vinay:** No, that's different. That is for, that is for... um, this, stat-related.

**Vikram:** Yep.

**Vinay:** Development. Market EQ risk ranking... I thought we had a Jira generated.

**Vikram:** Yep.

**Vinay:** But I'll walk you through it first. So, see, this is the file that we are supposed to be building.

**Vikram:** Mhm.

**Vinay:** Okay? This is the structure of the file that we are supposed to be building out. In this... I don't know, have you already built this out, or not?

**Vikram:** Uh, no, Vinay.

**Vinay:** This was in the performance case as well, right?

**Vikram:** Uh, Market EQ...

**Vinay:** Did we do it?

**Vikram:** FI EQ risk ranking...

**Vinay:** No, not FI.

**Vikram:** Uh, one second.

**Vinay:** I think this is just Market EQ risk ranking.

**Vikram:** Uh... Market EQ risk ranking, yes, Vinay, we already have that.

**Vinay:** Yeah, we have it, right? Where... Like, we have already deployed that, correct?

**Vikram:** In production, yes.

**Vinay:** Yeah. So... in that, currently all of these columns are empty?

**Vikram:** Yes.

**Vinay:** All the ranking ones?

**Vikram:** Yeah, only standard deviation I think we are trying to get from upstream, the rest are all empty.

**Vinay:** Alright. But I think for the rest, we just need a name from Manikanta, correct?

**Vikram:** Yeah.

**Vinay:** So that is fine. So only these ones... And is there a file already that I can look into?

**Vikram:** Uh, yes, Vinay.

**Vinay:** It should be in our prod folder, right?

**Vikram:** Yep.

**Vinay:** Prod... ACM... We don't need Rolling Perf. Is it this one? ACM Market EQ Risk Ranking.

**Vikram:** Yep.

**Vinay:** Do we have to do all this?

**Vikram:** Uh, select the first icon. Yeah.

**Vinay:** Alright. So ultimately, this file... what are you getting here? Uh...

**Vikram:** For a given composite ID, uh both uh...

**Vinay:** Like... here are six. 1, 2, 3, 4, 5, 6. Are we getting the same ones here too? 1, 2, 3, 4, 5, 6. Are all the names and numbers the same?

**Vikram:** Yes, yes.

**Vinay:** So if we filter on one... If I filter on... one of them... First, this High Quality Small Cap came separately. And then High Quality... Why did we do it like this? Is SMA not written in its name?

**Vikram:** Uh...

**Vinay:** Or should SMA be at the front/end of the name?

**Vikram:** Yes, yes, uh there are a few things. When I did some analysis, we have to make some changes too.

**Vinay:** Please get this done.

**Vikram:** Yeah, yeah, I'll do it.

**Vinay:** This is urgent. Where are all these changes listed?

**Vikram:** Uh, I have a list with me with the changes needed at our end. In this file...

**Vinay:** So this...

**Vikram:** ...this is the only change, and...

**Vinay:** Yeah. What name? The benchmark name isn't matching here either?

**Vikram:** Uh, yes. So these benchmark names are impacting across all the feeds. Uh, I'll do that too.

**Vinay:** Alright. Both will be gross, both will be this. Its name should change. And okay, both benchmark gross, alright? So now from this, these ones—columns F, H is fine, J, and L—we will get those from there, uh from the performance service. But these other ones, whatever rank it is: Downside Capture Rank, Standard Deviation Rank, Tracking Error Rank, and Alpha Rank. Okay? 1, 2, 3, 4, and lastly, Observation. For all these columns, we have to pick the data from this file.

**Vikram:** Okay.

**Vinay:** Got it?

**Vikram:** So the key would be an ID or a strategy name? Because I only see names here. So, this means that...

**Vinay:** You won't get an ID. You won't get an ID here.

**Vikram:** So, we have to make sure the names match between these files.

**Vinay:** Yeah, meaning like... let's do one thing. Edited it... So, now selected High Quality. Alright. In High Quality Select, you are getting these three different strategies. So I think we'll have to read through this. Let me confirm this once, like this High Quality... So she has provided the benchmark association. Let's look. See, here she has given the benchmark association for each of your High Quality Calvert Equity. This is its fund, the normal one, and this is its SMA one.

**Vikram:** Yep.

**Vinay:** Right? And then for this one, this is the index. For this one, this is the index. Now my point is, do we really need both indices?

**Vikram:** Those are the same index, right? Yes.

**Vinay:** Yeah, though it's the same index, here I think we are just showing one.

**Vikram:** Yes.

**Vinay:** So will this work? Like, how can we pull... For High Quality Calvert Equity, how can we pull just one index? Mhm.

**Vikram:** Uh so, Vinay, one more thing. Here the name says "High Quality Calvert Equity". If you just go back to the ACM reference file, what is the name over there?

**Vinay:** It is High Quality Calvert Equity.

**Vikram:** Calvert Equity. Okay. And for SMA, in this file, we are getting it at the end, but in ACM reference, is it at the start?

**Vinay:** So look, we are getting it like this here.

**Vikram:** Uh no no, check the file on the left side. I want to see the reference...

**Vinay:** Okay, here too it is on the right side. Okay.

**Vinay:** I think that name kind of matches, right?

**Vikram:** Yes.

**Vinay:** So...

**Vikram:** And the benchmark name is different. Uh Russell 1000 and there is like something only...

**Vinay:** Russell 1000 Growth Index. This is what we are receiving. And this... is what they are receiving. I think they are the ones showing the trademark or registered symbols.

**Vikram:** Yep.

**Vinay:** Alright, but I think you will be able to derive that, right?

**Vikram:** Uh so, I had this question...

**Vinay:** That like... Yeah.

**Vikram:** Should we be able to get this trademark name into the file, or is it fine if we skip those and use this name, like we have in the file on the right?

**Vinay:** No, I can confirm that. I can confirm that. But irrespective of that, if we...

**Vikram:** Yeah, I can...

**Vinay:** ...just focus on this one, like High Quality Calvert Equity, for which you are getting these four records. Okay? We'll match with the entity name. Its entity name is this, High Calvert Equity. So this matches with your column B ultimately. Or you could do another thing: against these entity names written here, you can create a map of portfolio symbols.

**Vikram:** Okay.

**Vinay:** So as you read this object and store all the values, while storing that object, you also look up in your mapping file and get the portfolio symbol code from there. This is nothing but the rep account.

**Vikram:** Yes.

**Vinay:** Got it? So you take this strategy name from here. With the strategy name, you know which rep account it belongs to.

**Vikram:** Uh so, you are suggesting to create a constants file to store the mapping for this field, Vinay?

**Vinay:** No, not really. Because we should already have all this data.

**Vikram:** Yeah yeah, we already have it. Okay.

**Vinay:** Right? And if you already know the name of the strategy... Do you have the database open? Check right now if it's there.

**Vikram:** Yeah yeah. We can pull it from that query itself.

**Vinay:** But are both present? Is the SMA one there as well?

**Vikram:** For SMA, we'll just append SMA at the end, Vinay.

**Vinay:** Okay. No, but how will you derive it? That was my question.

**Vinay:** Are you finding it in the database?

**Vikram:** Uh one second, yeah.

**Vikram:** Uh yes, Vinay. We are getting the name.

**Vinay:** Is the SMA one showing up too? Is that like a separate strategy or what is it? Ultimately, these are two composites, correct? We are getting the data for two composites: Composite 1 and Composite 2. So my question is, if you receive a file like this, will you be able to derive the rep account ID associated with the strategy for those composites?

**Vikram:** Yes, I will be able to derive the rep account ID for that strategy.

**Vinay:** And is our composite name written like this in the file?

**Vikram:** No, no, no. We're not maintaining those composites. We maintain the strategy associated with that composite, right? So SMA would just be appending SMA at the end.

**Vinay:** Okay. So ultimately from your side, you will only bring six strategies from your referential table.

**Vikram:** Yep.

**Vinay:** And again, this will happen at runtime where you just fetch all the equity strategies, their associated rep accounts, and the names of the strategies.

**Vikram:** Yep.

**Vinay:** Then as you go through the list, like when High Quality Calvert Equity comes up, you try to match this name with your map. It will give you the rep account as A799. You'll associate this with A799.

**Vikram:** Yep.

**Vinay:** Similarly for this one. Actually, you will match this column, right?

**Vikram:** Yes.

**Vinay:** Entity name. Yeah, so if it's like this, it will remain the same because again you won't find anything specifically as SMA, so you can probably ignore that. But you use the same name here to get the same portfolio ID.

**Vikram:** Yep.

**Vinay:** Correct? And then... when the benchmark comes in, I think at the initial stage you can just associate these individually, because this again is part of... Actually, you can do this: read the strategy names, and if a strategy name matches with your map, associate an account ID with it—in this case, A799. Do the same for all the other categories. So at the end, you'll have a complete map where, for each portfolio ID, you will have a list of portfolio IDs, a list of entity names to use, and all the ranking information from these columns. Got it? Then you can probably put this into a map because when you insert it into a map, you'll only use High Quality Calvert Equity and the account ID as the key. Alright? Entity name and account ID as the key. With this and this as the key, each will be unique. But when you try to insert the benchmark data, it might attempt it twice, though when inserting into a list, it will only insert once.

**Vikram:** Yep.

**Vinay:** Correct? That way your duplicates will also get eliminated. Correct. And once you get all this data, you maintain it in one of your steps, because to generate this file, you need data from two sources: one is the eVestment manual file and the other is your regular performance file.

**Vikram:** Yep.

**Vinay:** So I think you'll have to integrate that.

**Vikram:** Both will be under the same job, but across two steps.

**Vinay:** Yeah, I think that's how we'll have to do it.

**Vikram:** Yeah.

**Vinay:** You've already completed this part, right? The first part.

**Vikram:** Yep.

**Vinay:** I think it is more about integrating the file you read from eVestment as part of step two, and populating those data points here in the ranking columns.

**Vikram:** Yes.

**Vinay:** So if this name matches—if you fix this name, right? If you fix this name, it will actually be much simpler because all you'll need to do is use this name and match it with the entity name column.

**Vikram:** Yep.

**Vinay:** Then you might not even need to build this map. So if you resolve that SMA issue, and if this is how your final output turns out, then this name and that name will match. And once they match, you simply pick the data from those columns (1, 2, 3, 4, 5) and populate it here.

**Vikram:** Okay.

**Vinay:** I think that would be much easier, right?

**Vikram:** Yes.

**Vinay:** So that works for your actual names. But for index, as of now, I don't know—there will be many for Russell.

**Vikram:** I think we have to maintain a map between the name and unique names or something.

**Vinay:** For this one?

**Vikram:** Yeah, so like strategy name plus entity name maps as a unique key if we can do that. Because...

**Vinay:** No, but all of this will remain the same, right? Look, the portfolio part is already solved, right? Because based on the strategy name itself in column B in your case, this name will match with your entity name in column C. Correct? So if it's Calvert Equity, this matches, and if it's SMA, that matches. So this happens automatically. You're getting this from your referential information file anyway, correct? So there's no need to repeat that step. Now that this matches the date—and there will also be an "As of date" which should also match—once both match, you simply pick the columns from here and place them into the respective ranking columns. These are all ranks, okay? And there is an Observation column at the end. You're able to fetch this anyway.

**Vikram:** Okay.

**Vinay:** So before you move to step two, your entire structure will already be built, right?

**Vikram:** Yeah.

**Vinay:** You will also have the index information, correct?

**Vikram:** Yep.

**Vinay:** Yeah. Now regarding the index, I'll ask them how they want it in the final file—whether they want the ® symbol included or not. I can check with them. But looking at this file as an example, this is your index information. And if you look through it, the data is essentially the same across everything. Or maybe not.

**Vikram:** Uh no.

**Vinay:** Maybe not.

**Vikram:** Yeah.

**Vinay:** Then... it will have to be handled separately for each.

**Vikram:** Yeah, so if you just do a unique mapping between column B and column C...

**Vinay:** Mhm.

**Vikram:** ...and apply the same across our...

**Vinay:** These two?

**Vikram:** Yeah.

**Vinay:** Okay, so what will you do here then?

**Vikram:** Here too, for a given strategy, we find what the mapped benchmark name is. If we can create a unique list of that, we can also eliminate duplicates, right?

**Vinay:** Mhm.

**Vikram:** Then we can just...

**Vinay:** Are you talking about here?

**Vikram:** No no no, from our database we can extract the strategy and the benchmark associated with it. If we create a map and a unique list of that, we will get four unique lists because we are only using four strategies here. We can just...

**Vinay:** No, there are six strategies in total, right?

**Vikram:** Yeah, so for six strategies we'll get six unique lists, but with the benchmark match...

**Vinay:** Combination, correct.

**Vikram:** ...combination, correct. Using that information, we can write the unique list into the feed file. So for each strategy, we will create a loop, and within that loop itself, we'll use that strategy and index combination to extract the data from this file. It will be six iterations from this file to pull the data and write it into the feed file.

**Vinay:** Alright, give that a try. See if that is the most optimized approach. But this is ultimately where you will pull the data from.

**Vikram:** Uh, yes, Vinay.

**Vinay:** And... and in this too...

**Vikram:** And do we get all these tabs every time?

**Vinay:** Yes. We only need to use the Template tab.

**Vikram:** Okay.

**Vinay:** Got it? I think I lost that file. Here it is, risk ranking. I think there was a date here... 6/30/2026. Yes, something like that, but you will receive this entire file. We just need to focus on this worksheet, which is Template.

**Vikram:** Okay.

**Vinay:** So we read data from here, and all headers will be on row number two.

**Vikram:** Okay.

**Vinay:** And this column will ideally be populated.

**Vikram:** Okay.

**Vinay:** Alright. That's one thing. Can you give that a try today and see how the output looks?

**Vikram:** Uh, okay, Vinay.

**Vinay:** Yeah. Let's connect again around 4:30.

**Vikram:** Mhm.

**Vinay:** To see how the output is coming along.

**Vikram:** Uh, okay.

**Vinay:** Let's quickly create a Jira ticket for this and tag it under the eVestment category.

**Vikram:** Mhm.

**Vinay:** And then use your DevTool to add a code snippet once you're done pushing attribution out.

**Vikram:** Uh, okay, Vinay.

**Vinay:** And if you're pushing, let's also push the SMA change.

**Vikram:** Uh, okay.

**Vinay:** Got it? Alright, so this was the other item. But please update me on this today itself. We need to wrap up this implementation as soon as possible.

**Vikram:** Uh, okay.

**Vinay:** Alright? Starting today, Vikram, everything will move very fast. Whatever work we focus on, we'll have to turn around quickly.

**Vikram:** Uh, okay, Vinay, sure.

**Vinay:** Um, I'm trying to bring up the other message you sent. What's in this? Through this message, first off, what is the accounts feed file?

**Vikram:** Yeah yeah. I should have named it properly. We have two types, right? We send the composite ID in the request...

**Vinay:** Correct.

**Vikram:** ...and we send the actual accounts in the request. So, actual...

**Vinay:** So let's cover the composite summary first. How many files do we...

**Vikram:** Uh, for accounts we only have one feed file.

**Vinay:** Where did it go?

**Vikram:** Actually there are two, but the second one is mostly AUM data. If you see, FI EQ and accounts...

**Vinay:** Which one is AUM? Look, what is this one?

**Vikram:** This is accounts.

**Vinay:** This is accounts only?

**Vikram:** Yep.

**Vinay:** Yeah. All the SMAs, correct?

**Vikram:** Yep. We could write it as rep accounts.

**Vinay:** It's not a rep account.

**Vikram:** Uh-huh, okay. Yeah.

**Vinay:** No no, are you saying this is all composite-level data, or rep accounts, or...

**Vikram:** Account-level data.

**Vinay:** SMA accounts, correct?

**Vikram:** Yes, yes.

**Vinay:** Look, they don't show the composite ID anyway. They show the rep account.

**Vikram:** Yep.

**Vinay:** So I just want to confirm, this contains data for 150 accounts?

**Vikram:** Yes.

**Vinay:** Equity accounts?

**Vikram:** Yes.

**Vinay:** Alright. And what about the rest...

**Vikram:** Uh, if you see Annualized Perf Accounts Summary...

**Vikram:** ...that is also all account-level data. But we are not sourcing anything from it for now because it contains mostly AUM data.

**Vinay:** Alright. So these are the only two, correct?

**Vikram:** Yep.

**Vinay:** Now tell me about the remaining ones—we discussed splitting them, has that split been done?

**Vikram:** Uh, the split is not yet done, Vinay. I'll do it.

**Vinay:** When did we discuss this, Vikram?

**Vikram:** Uh, actually...

**Vinay:** Thursday?

**Vikram:** Yes.
