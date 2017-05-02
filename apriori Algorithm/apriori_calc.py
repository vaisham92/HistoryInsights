import breakData
import generateRules

urlSet, transactionList = breakData.break_data()
print urlSet
print ".................."
print transactionList
items, rules = generateRules.generate_rules(urlSet, transactionList, 0.17, 0.8)
generateRules.printResults(items, rules)