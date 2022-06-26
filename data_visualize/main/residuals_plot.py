import matplotlib.pyplot as plot_residual

dataStr = open("../files/residual.txt", "r").readlines()

x_residual = []
y_residual = []

for i in range(len(dataStr)):
    dataStr[i] = dataStr[i].replace("\n", "")
    temp = dataStr[i].split("\t")
    x_residual.append(float(temp[0]))
    y_residual.append(float(temp[1]))

plot_residual.figure(figsize=(20, 8), dpi=280)
plot_residual.xlabel("手机使用时长（小时）", fontsize=22)
plot_residual.ylabel("残差", fontsize=22)
plot_residual.title("手机使用时长与网购次数的关系残差图", fontsize=24)
plot_residual.rcParams['font.sans-serif'] = ['KaiTi']
plot_residual.rcParams['axes.unicode_minus'] = False
plot_residual.tick_params(axis='both', which='major', labelsize=20)
plot_residual.scatter(x_residual, y_residual, s=20, c="r")

plot_residual.show()
#plot_residual.savefig("..\\files\\residual_plot.png")