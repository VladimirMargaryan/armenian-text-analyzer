
import sys
import stanza

text = str(sys.argv[1])

def lemmatize(text):
	nlp = stanza.Pipeline(lang='hy', processors='tokenize,mwt,pos,lemma,depparse')
	doc = nlp(text)
	out = []

	for text in doc.sentences:
		out.append(text.words)
	return out

print(lemmatize(text))