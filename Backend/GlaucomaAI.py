# Source: Pytorch CNN Tutorial -> https://pytorch.org/tutorials/beginner/blitz/cifar10_tutorial.html
import torch
import torchvision
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as O
import torch.utils.data as data
from torch.autograd import Variable
import torchvision
from torchvision import transforms
import numpy as np
import matplotlib.pyplot as plt

EPOCHS = 2
BATCH_SIZE = 10
LEARNING_RATE = 0.001
TRAIN_DATA_PATH = "./images/train/"
PREDICT_DATA_PATH = "./images/predict/"
TRANSFORM_IMG = transforms.Compose([
    transforms.Resize(32),
    transforms.CenterCrop(32),
    transforms.ToTensor(),
    transforms.Normalize((0.5, 0.5, 0.5), (0.5, 0.5, 0.5))
    ])

train_data = torchvision.datasets.ImageFolder(root=TRAIN_DATA_PATH, transform=TRANSFORM_IMG)
train_data_loader = data.DataLoader(train_data, batch_size=BATCH_SIZE, shuffle=True)
predict_data = torchvision.datasets.ImageFolder(root=PREDICT_DATA_PATH, transform=TRANSFORM_IMG)
predict_data_loader  = data.DataLoader(predict_data, batch_size=BATCH_SIZE, shuffle=True)

classes = ('Glaucoma', 'Healthy')

class GlacomaCNN(nn.Module):
    def __init__(self):
        super(GlacomaCNN, self).__init__()

        self.conv1 = nn.Conv2d(3, 6, 5)
        self.pool = nn.MaxPool2d(2, 2)
        self.conv2 = nn.Conv2d(6, 16, 5)
        self.fc1 = nn.Linear(16*5*5, 140)
        self.fc2 = nn.Linear(140, 64)
        self.fc3 = nn.Linear(64, 2)

    def forward(self, x):

        x = self.pool(F.relu(self.conv1(x)))
        x = self.pool(F.relu(self.conv2(x)))
        x = x.view(-1, 16*5*5)
        x = F.relu(self.fc1(x))
        x = F.relu(self.fc2(x))
        x = self.fc3(x)

        return x

def create_loss():
    return nn.CrossEntropyLoss()

def create_optimizer(net: GlacomaCNN, learning_rate = LEARNING_RATE):
    return O.Adam(net.parameters(), lr=learning_rate)

def train(net:GlacomaCNN, epochs = EPOCHS, learning_rate = LEARNING_RATE):

    loss = create_loss()

    optimizer = create_optimizer(net, learning_rate)

    for epoch in range(epochs):
        for i, (inputs, labels) in enumerate(train_data_loader, 0):

            # inputs = x
            # labels = y

            # print(step,"\n", inputs,"\n", labels)

            # optimizer.zero_grad()

            outputs = net(inputs)
            loss_size = loss(outputs, labels)
            loss_size.backward()
            optimizer.step()


if __name__ == "__main__":
    model = GlacomaCNN()
    train(model)
    testiter = iter(predict_data_loader)
    images, labels = testiter.next()
    outputs = model(images)
    notuseful, predictions = torch.max(outputs, 1)
    print("Prediction(s):")
    print(' '.join(classes[predictions[i]] for i in range(len(predictions))))