# Task 2 Evaluation results using actual Top N business and predicted Top N business at a given location:

# required imports for accuracy and f1-score
from sklearn import metrics
from sklearn.metrics import confusion_matrix
from sklearn.metrics import accuracy_score
from sklearn.metrics import f1_score


def accuracy_calculation(actual_topN, predicted_topN):
    accuracy = (metrics.accuracy_score(actual_topN, predicted_topN)) * 100
    f1_score = f1_score(actual_topN, predicted_topN, average='weighted')

    return accuracy, f1_score

