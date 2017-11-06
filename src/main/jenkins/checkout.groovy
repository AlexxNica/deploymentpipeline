
def gitCheckout() {
	stage 'Checkout GIT'
	git branch: "${GIT_BRANCH}", url: "${GIT_URL}"
}
return this