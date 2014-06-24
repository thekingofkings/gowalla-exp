condition = 2;

data = importdata('../data/CDF-measures-t5000.txt');
data = data(data(:,6) > condition, :);

%% G1 is the sum of personal weight
% weight, frequency, friend label
fri_g1 = data(data(:,9)==1, 8) ./ data(data(:,9)==1, 6);
nonfri_g1 = data(data(:,9)==0, 8)./ data(data(:,9)==0, 6);


figure();
hold on;
box on;
grid off;
l1 = cdfplot(fri_g1(:,1));
l2 = cdfplot(nonfri_g1(:,1));
set(l1, 'color', 'blue', 'linestyle', '-');
set(l2, 'color', 'red', 'linestyle', '--');

hline = findobj(gcf, 'type', 'line');
set(hline, 'linewidth', 3);
axis([10^-2, 10^4, 0, 1]);
% axis([10^-1, 7, -0.01, 1.01]);
title('');
ylabel('CDF', 'fontsize', 20);
xlabel('$G_1(E_{ij})/|E_{ij}|$', 'fontsize', 24, 'interpreter', 'latex');
set(gca,  'fontsize', 20, 'linewidth', 3, 'xscale', 'log');
legend( {'Friend', 'Non-friend'}, 'location', 'southeast');
print('gmean.eps', '-dpsc');
system('epstopdf gmean.eps');


%% G2 is the min of personal weight
% weight, frequency, friend label
fri_g2 = data(data(:,9)==1, 5);
nonfri_g2 = data(data(:,9)==0, 5);


figure();
hold on;
box on;
grid on;
l1 = cdfplot(fri_g2(:,1));
l2 = cdfplot(nonfri_g2(:,1));
set(l1, 'color', 'blue', 'linestyle', '-');
set(l2, 'color', 'red', 'linestyle', '--');

hline = findobj(gcf, 'type', 'line');
set(hline, 'linewidth', 3);
% axis([10^-1, 7, -0.01, 1.01]);
axis([10^-2, 10^4, 0, 1]);
title('');
ylabel('CDF', 'fontsize', 20);
xlabel('$G_2(E_{ij})/|E_{ij}|$', 'fontsize', 24, 'interpreter', 'latex');
set(gca, 'xscale', 'log', 'fontsize', 20, 'linewidth', 3);
legend( {'Friend', 'Non-friend'}, 'location', 'northwest');
print('gmin.eps', '-dpsc');
system('epstopdf gmin.eps');



%% G3 is the combine of personal + global
% weight, frequency, friend label
fri_g3 = data(data(:,9)==1, 3);
nonfri_g3 = data(data(:,9)==0, 3);


figure();
hold on;
box on;
grid on;
l1 = cdfplot(fri_g3(:,1));
l2 = cdfplot(nonfri_g3(:,1));
set(l1, 'color', 'blue', 'linestyle', '-');
set(l2, 'color', 'red', 'linestyle', '--');

hline = findobj(gcf, 'type', 'line');
set(hline, 'linewidth', 3);
% axis([10^(-3), 10^1, -0.01, 1.01]);
axis([10^-2, 10^4, 0, 1]);
title('');
ylabel('CDF', 'fontsize', 20);
xlabel('$G_3(E_{ij})/|E_{ij}|$', 'fontsize', 24, 'interpreter', 'latex');
set(gca,'xscale', 'log',  'fontsize', 20, 'linewidth', 3 ) %, ...
    %'xtick', [10^-3, 10^-2, 10^-1, 10^0, 10^1]);
legend( {'Friend', 'Non-friend'}, 'location', 'southeast');
set(gcf,'PaperUnits', 'inches');
print('g2.eps', '-dpsc');
system('epstopdf g2.eps');



%% G is the combine of three -- personal + global + temporal
% weight, frequency, friend label
fri_g = data(data(:,9)==1, 7);
nonfri_g = data(data(:,9)==0, 7);


figure();
hold on;
box on;
l1 = cdfplot(fri_g(:,1));
l2 = cdfplot(nonfri_g(:,1));
set(l1, 'color', 'blue', 'linestyle', '-');
set(l2, 'color', 'red', 'linestyle', '--');

hline = findobj(gcf, 'type', 'line');
set(hline, 'linewidth', 3);
axis([10^-2, 10^4, 0, 1]);
title('');
ylabel('CDF', 'fontsize', 20);
xlabel('$G(E_{ij})/|E_{ij}|$', 'fontsize', 24, 'interpreter', 'latex');
set(gca,  'fontsize', 20, 'linewidth', 3, 'xscale', 'log') %, ...
 %'xtick', [10^-3, 10^-2, 10^-1, 10^0, 10^1]);
legend( {'Friend', 'Non-friend'}, 'location', 'southeast');
print('g3.eps', '-dpsc');
system('epstopdf g3.eps');

