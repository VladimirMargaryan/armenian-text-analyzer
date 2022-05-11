
import sys
import stanza

text = str(sys.argv[1])

def lemmatize(text):
	nlp = stanza.Pipeline(lang='hy', processors='tokenize,lemma,pos,depparse')
	doc = nlp(text)
	out = []

	for text in doc.sentences:
		tokens = []
		# for word in text.words:
		# 	# if word.upos in allowed_postags:
		# 	# tokens.append(word.pos)
		# 	tokens.append(word.text)
		# 	# tokens.append(word.lemma)
		# final = " ".join(tokens)
		out.append(text.words)
	return out

# coding=utf-8

print(lemmatize(text))