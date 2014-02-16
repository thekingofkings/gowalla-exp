% the distribution of prabability and entropy of one meeting event

%{
% load the data first
dml4 = importdata('../weightedFrequency-1000u.txt');
[~, ind] = sort(dml4(:,4));
dml4 = dml4(ind, :);
dml5 = importdata('../delete_this-u1000c1.50000.txt');
[~, ind] = sort(dml5(:,6));
dml5 = dml5(ind, :);

omc = dml5;
omc(:,4) = dml4(:,3);

% ua, ub, product, location entropy, 1 - exp, frequency, friend flag

% select single meeting pair
omc = omc(omc(:,6)==1,:);

% construct interesting matrix
%   [location entropy,  1 - exp, friend flag]
interesting = [omc(:,4), omc(:,5), omc(:,7)];
interest_fri = interesting( interesting(:,3)==1,:);
interest_nonfri = interesting(interesting(:,3)==0,:);
fri_entro = interest_fri(:,1);
fri_denst = interest_fri(:,2);
nonfri_entro = interest_nonfri(:,1);
nonfri_denst = interest_nonfri(:,2);
%}


for freq = 1:5

omc = importdata('../dist-meeting-cases-u5000.txt');
omc = omc(omc(:,5)==freq,:);
n = size(omc,2);
interest_fri = omc(omc(:,n)==1,:);
interest_nonfri = omc(omc(:,n)==0,:);
fri_entro = interest_fri(:,2);
fri_denst = interest_fri(:,1);
fri_measure = interest_fri(:,3);
fri_entroW = interest_fri(:,4);
nonfri_entro = interest_nonfri(:,2);
nonfri_denst = interest_nonfri(:,1);
nonfri_measure = interest_nonfri(:,3);
nonfri_entroW = interest_nonfri(:,4);

% fri_entro = reshape(interest_fri(:,1:(n-1)/2), numel(interest_fri(:,1:(n-1)/2)), 1);
% fri_denst = reshape(interest_fri(:,(n+1)/2:n-1), numel(interest_fri(:,(n+1)/2:n-1)), 1);
% 
% nonfri_entro = reshape(interest_nonfri(:,1:(n-1)/2), numel(interest_nonfri(:,1:(n-1)/2)), 1);
% nonfri_denst = reshape(interest_nonfri(:,(n+1)/2:n-1), numel(interest_nonfri(:,(n+1)/2:n-1)), 1);


f = figure();
hold on;
% l1 = cdfplot(fri_entro);
% l2 = cdfplot(nonfri_entro);
% set(l1, 'color', 'blue', 'linestyle', '-');
% set(l2, 'color', 'red', 'linestyle', '-');


l3 = cdfplot(fri_denst);
l4 = cdfplot(nonfri_denst);
set(l3, 'color', 'cyan', 'linestyle', '--');
set(l4, 'color', 'magenta', 'linestyle', '--');

% 
% l5 = cdfplot(fri_entroW);
% l6 = cdfplot(nonfri_entroW);
% set(l5, 'color', 'blue', 'linestyle', '--');
% set(l6, 'color', 'red', 'linestyle', '--');
% 
% 
% l7 = cdfplot(fri_measure);
% l8 = cdfplot(nonfri_measure);
% set(l7, 'color', 'yellow', 'linestyle', '-');
% set(l8, 'color', 'green', 'linestyle', '-');


hline = findobj(gcf, 'type', 'line');
set(hline, 'linewidth', 3);
ylabel('CDF', 'fontsize', 16);
xlabel('Location entropy (solid) / Density (dashed)', 'fontsize', 16);
set(gca, 'xscale', 'log');
legend({ 'Fri entropy', 'Nonfri entropy', ...
    'Fri density', 'Nonfri density', ...
    'Fri entro weight', 'Nonfri entro weight', 'Fri - log', 'Nonfri - log'}, ...
    'location', 'best', 'fontsize', 12);
title(['Distribution of == ', num2str(freq), ' Meeting Pair (friends/non-friends)'], 'fontsize', 16);
set(gca, 'fontsize', 12);
 saveas(f, ['dist-MeetingDistc1p5EQavg', num2str(freq), '.png']);
% saveas(f, 'MeetingDist.fig');

end
