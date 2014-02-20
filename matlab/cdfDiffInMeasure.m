condition = 1;

%% G1 is the sum of personal weight
% weight, frequency, friend label
g1 = importdata('../Pair-sum-measure.txt');
g1(:,1) = g1(:,1) ./ g1(:,2);
g1 = g1(g1(:,2) > condition,:);
fri_g1 = g1(g1(:,3)==1, :);
nonfri_g1 = g1(g1(:,3)==0, :);


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
% axis([10^0, 10^2, 0, 1]);
axis([10^-1, 7, -0.01, 1.01]);
title('');
ylabel('CDF', 'fontsize', 20);
xlabel('$G_1(E_{ij})/|E_{ij}|$', 'fontsize', 24, 'interpreter', 'latex');
set(gca,  'fontsize', 18, 'linewidth', 2);
legend( {'Friend', 'Non-friend'}, 'location', 'northwest');
print('g1.eps', '-dpsc');
system('epstopdf g1.eps');


%% G2 is the min of personal weight
% weight, frequency, friend label
g2 = importdata('../pair-min-measure.txt');
g2(:,1) = g2(:,1) ./ g2(:,2);
g2 = g2(g2(:,2) > condition,:);
fri_g2 = g2(g2(:,3)==1, :);
nonfri_g2 = g2(g2(:,3)==0, :);


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
axis([10^-1, 7, -0.01, 1.01]);
title('');
ylabel('CDF', 'fontsize', 20);
xlabel('$G_2(E_{ij})/|E_{ij}|$', 'fontsize', 24, 'interpreter', 'latex');
set(gca, 'xscale', 'log', 'fontsize', 18, 'linewidth', 2);
legend( {'Friend', 'Non-friend'}, 'location', 'northwest');
print('g2.eps', '-dpsc');
system('epstopdf g2.eps');



%% G3 is the global weight
% weight, frequency, friend label
g3 = importdata('../Pair-prod-measure.txt');
g3(:,1) = g3(:,1) ./ g3(:,2);
g3 = g3(g3(:,2) > condition ,:);
fri_g3 = g3(g3(:,3)==1, :);
nonfri_g3 = g3(g3(:,3)==0, :);


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
axis([10^(-3), 10^1, -0.01, 1.01]);
title('');
ylabel('CDF', 'fontsize', 20);
xlabel('$\overline{w}_{ij}^g(e_k)$', 'fontsize', 24, 'interpreter', 'latex');
set(gca,'xscale', 'log',  'fontsize', 18, 'linewidth', 2 ) %, ...
    %'xtick', [10^-3, 10^-2, 10^-1, 10^0, 10^1]);
legend( {'Friend', 'Non-friend'}, 'location', 'southeast');
set(gcf,'PaperUnits', 'inches');
print('g3.eps', '-dpsc');
system('epstopdf g3.eps');



%% G is the sum of personal weight
% weight, frequency, friend label
g = importdata('../Pair-time-measure.txt');
g(:,1) = g(:,1) ./ g(:,2);
g = g(g(:,2) > condition,:);
fri_g = g(g(:,3)==1, :);
nonfri_g = g(g(:,3)==0, :);


figure();
hold on;
box on;
l1 = cdfplot(fri_g(:,1));
l2 = cdfplot(nonfri_g(:,1));
set(l1, 'color', 'blue', 'linestyle', '-');
set(l2, 'color', 'red', 'linestyle', '--');

hline = findobj(gcf, 'type', 'line');
set(hline, 'linewidth', 3);
% axis([10^(-3), 10^1, -0.01, 1.01]);
title('');
ylabel('CDF', 'fontsize', 20);
xlabel('$\overline{w}_{ij}^t(e_k)$', 'fontsize', 24, 'interpreter', 'latex');
set(gca,  'fontsize', 18, 'linewidth', 2) %, ...
 %'xtick', [10^-3, 10^-2, 10^-1, 10^0, 10^1]);
legend( {'Friend', 'Non-friend'}, 'location', 'southeast');
print('g.eps', '-dpsc');
system('epstopdf g.eps');

